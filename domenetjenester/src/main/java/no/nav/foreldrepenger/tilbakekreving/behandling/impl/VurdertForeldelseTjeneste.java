package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeMedBeløpDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagEndretEvent;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;

@ApplicationScoped
@Transactional
public class VurdertForeldelseTjeneste {

    private VurdertForeldelseRepository vurdertForeldelseRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private BehandlingRepository behandlingRepository;

    private HistorikkTjenesteAdapter historikkTjenesteAdapter;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste;

    VurdertForeldelseTjeneste() {
        // For CDI
    }

    @Inject
    public VurdertForeldelseTjeneste(BehandlingRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkTjenesteAdapter, KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste) {
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.vurdertForeldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();

        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
        this.kravgrunnlagBeregningTjeneste = kravgrunnlagBeregningTjeneste;
    }

    public void lagreVurdertForeldelseGrunnlag(Long behandlingId, List<ForeldelsePeriodeDto> foreldelsePerioder) {
        VurdertForeldelse vurdertForeldelse = new VurdertForeldelse();
        for (ForeldelsePeriodeDto foreldelsePeriodeDto : foreldelsePerioder) {
            VurdertForeldelsePeriode vurdertForeldelsePeriode = lagVurdertForeldelse(vurdertForeldelse, foreldelsePeriodeDto);
            vurdertForeldelse.leggTilVurderForeldelsePerioder(vurdertForeldelsePeriode);
        }
        Optional<VurdertForeldelse> forrigeVurdertForeldelse = vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId);
        vurdertForeldelseRepository.lagre(behandlingId, vurdertForeldelse);
        lagInnslag(behandlingId, forrigeVurdertForeldelse, vurdertForeldelse);
    }

    public FeilutbetalingPerioderDto hentFaktaPerioder(Long behandlingId) {
        Optional<FaktaFeilutbetaling> fakta = faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(behandlingId);
        FeilutbetalingPerioderDto perioderDto = new FeilutbetalingPerioderDto();
        perioderDto.setBehandlingId(behandlingId);
        List<ForeldelsePeriodeMedBeløpDto> perioder = new ArrayList<>();
        if (fakta.isPresent() && !fakta.get().getFeilutbetaltPerioder().isEmpty()) {
            for (FaktaFeilutbetalingPeriode faktaFeilutbetalingPeriode : fakta.get().getFeilutbetaltPerioder()) {
                ForeldelsePeriodeMedBeløpDto periode = new ForeldelsePeriodeMedBeløpDto();
                periode.setPeriode(faktaFeilutbetalingPeriode.getPeriode());
                periode.setForeldelseVurderingType(ForeldelseVurderingType.UDEFINERT);
                perioder.add(periode);
            }

            List<Periode> råPerioder = perioder.stream().map(ForeldelsePeriodeMedBeløpDto::tilPeriode).collect(Collectors.toList());
            Map<Periode, BigDecimal> feilPrPeriode = kravgrunnlagBeregningTjeneste.beregnFeilutbetaltBeløp(behandlingId, råPerioder);

            for (ForeldelsePeriodeMedBeløpDto foreldelsePeriodeMedBeløpDto : perioder) {
                foreldelsePeriodeMedBeløpDto.setBelop(feilPrPeriode.get(foreldelsePeriodeMedBeløpDto.tilPeriode()));
            }

            perioderDto.setPerioder(perioder);
        }
        return perioderDto;
    }

    public FeilutbetalingPerioderDto henteVurdertForeldelse(Long behandlingId) {
        Optional<VurdertForeldelse> vurdertForeldelseOpt = vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId);
        FeilutbetalingPerioderDto feilutbetalingPerioderDto = new FeilutbetalingPerioderDto();
        if (vurdertForeldelseOpt.isPresent()) {
            VurdertForeldelse vurdertForeldelse = vurdertForeldelseOpt.get();
            List<Periode> vfPerioder = vurdertForeldelse.getVurdertForeldelsePerioder().stream().map(VurdertForeldelsePeriode::getPeriode).collect(Collectors.toList());
            Map<Periode, BigDecimal> feilutbetaltBeløpPrPeriode = kravgrunnlagBeregningTjeneste.beregnFeilutbetaltBeløp(behandlingId, vfPerioder);
            List<ForeldelsePeriodeMedBeløpDto> resultat = new ArrayList<>();
            List<VurdertForeldelsePeriode> foreldelsePerioder = new ArrayList<>(vurdertForeldelse.getVurdertForeldelsePerioder());
            foreldelsePerioder.sort(Comparator.comparing(VurdertForeldelsePeriode::getFom));
            for (VurdertForeldelsePeriode vurdertForeldelsePeriode : foreldelsePerioder) {
                ForeldelsePeriodeMedBeløpDto fmfPeriodeDto = new ForeldelsePeriodeMedBeløpDto();
                Periode periode = vurdertForeldelsePeriode.getPeriode();
                fmfPeriodeDto.setPeriode(periode);
                fmfPeriodeDto.setForeldelseVurderingType(vurdertForeldelsePeriode.getForeldelseVurderingType());
                fmfPeriodeDto.setForeldelsesfrist(vurdertForeldelsePeriode.getForeldelsesfrist());
                fmfPeriodeDto.setOppdagelsesDato(vurdertForeldelsePeriode.getOppdagelsesDato());
                fmfPeriodeDto.setBegrunnelse(vurdertForeldelsePeriode.getBegrunnelse());
                fmfPeriodeDto.setBelop(feilutbetaltBeløpPrPeriode.get(periode));
                resultat.add(fmfPeriodeDto);
            }
            feilutbetalingPerioderDto.setPerioder(resultat);
        }
        return feilutbetalingPerioderDto;
    }

    public boolean harVurdertForeldelse(Long behandlingId) {
        return vurdertForeldelseRepository.harVurdertForeldelseForBehandlingId(behandlingId);
    }

    public void slettGammelForeldelseData(@Observes KravgrunnlagEndretEvent event) {
        vurdertForeldelseRepository.slettForeldelse(event.getBehandlingId());
    }

    private VurdertForeldelsePeriode lagVurdertForeldelse(VurdertForeldelse vurdertForeldelse, ForeldelsePeriodeDto foreldelsePeriodeDto) {
        VurdertForeldelsePeriode.Builder periodeBuilder = VurdertForeldelsePeriode.builder()
                .medVurdertForeldelse(vurdertForeldelse)
                .medForeldelseVurderingType(foreldelsePeriodeDto.getForeldelseVurderingType())
                .medPeriode(foreldelsePeriodeDto.getFraDato(), foreldelsePeriodeDto.getTilDato())
                .medBegrunnelse(foreldelsePeriodeDto.getBegrunnelse());
        if (ForeldelseVurderingType.FORELDET.equals(foreldelsePeriodeDto.getForeldelseVurderingType())) {
            periodeBuilder
                    .medForeldelsesFrist(foreldelsePeriodeDto.getForeldelsesfrist());
        } else if (ForeldelseVurderingType.TILLEGGSFRIST.equals(foreldelsePeriodeDto.getForeldelseVurderingType())) {
            periodeBuilder
                    .medForeldelsesFrist(foreldelsePeriodeDto.getForeldelsesfrist())
                    .medOppdagelseDato(foreldelsePeriodeDto.getOppdagelsesDato());
        }
        return periodeBuilder.build();
    }


    private void lagInnslag(Long behandlingId, Optional<VurdertForeldelse> forrigeVurdertForeldelse, VurdertForeldelse vurdertForeldelseAggregate) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FORELDELSE);
        historikkinnslag.setBehandlingId(behandlingId);
        historikkinnslag.setAktør(behandling.isAutomatiskSaksbehandlet() ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER);

        boolean behovForHistorikkInnslag = false;
        for (VurdertForeldelsePeriode foreldelsePeriode : vurdertForeldelseAggregate.getVurdertForeldelsePerioder()) {
            HistorikkInnslagTekstBuilder tekstBuilder = historikkTjenesteAdapter.tekstBuilder();
            boolean harEndret;
            // forrigeVurdertForeldelse finnes ikke
            if (forrigeVurdertForeldelse.isEmpty()) {
                harEndret = true;
                lagNyttInnslag(foreldelsePeriode, tekstBuilder);
            } else {
                harEndret = opprettInnslagNårForrigePerioderFinnes(behandlingId, forrigeVurdertForeldelse.get(), foreldelsePeriode, tekstBuilder);
            }
            if (harEndret) {
                tekstBuilder.medSkjermlenke(SkjermlenkeType.FORELDELSE)
                        .medOpplysning(HistorikkOpplysningType.PERIODE_FOM, foreldelsePeriode.getPeriode().getFom())
                        .medOpplysning(HistorikkOpplysningType.PERIODE_TOM, foreldelsePeriode.getPeriode().getTom())
                        .medBegrunnelse(foreldelsePeriode.getBegrunnelse());

                tekstBuilder.build(historikkinnslag);
                behovForHistorikkInnslag = true;
            }
        }

        if (behovForHistorikkInnslag) {
            historikkTjenesteAdapter.lagInnslag(historikkinnslag);
        }

    }

    private boolean opprettInnslagNårForrigePerioderFinnes(Long behandlingId,
                                                           VurdertForeldelse forrigeVurdertForeldelse,
                                                           VurdertForeldelsePeriode foreldelsePeriode,
                                                           HistorikkInnslagTekstBuilder tekstBuilder) {

        Optional<VurdertForeldelsePeriode> forrigeForeldelsePeriode = forrigeVurdertForeldelse.getVurdertForeldelsePerioder()
                .stream()
                .filter(vurdertForeldelsePeriode -> vurdertForeldelsePeriode.getPeriode().equals(foreldelsePeriode.getPeriode()))
                .findAny();
        boolean harEndret = false;
        // samme perioder med endret foreldelse vurdering type
        if (forrigeForeldelsePeriode.isPresent()) {
            harEndret = lagEndretInnslag(behandlingId, foreldelsePeriode, tekstBuilder, forrigeForeldelsePeriode.get());
            harEndret = harEndret || !foreldelsePeriode.getBegrunnelse().equals(forrigeForeldelsePeriode.get().getBegrunnelse());
        } else { // nye perioder
            harEndret = true;
            lagNyttInnslag(foreldelsePeriode, tekstBuilder);
            // hvis saksbehandler deler opp perioder, må vi starte vilkårs på nytt
            sletteVilkårData(behandlingId);
        }
        return harEndret;
    }

    private boolean lagEndretInnslag(Long behandlingId,
                                     VurdertForeldelsePeriode foreldelsePeriode,
                                     HistorikkInnslagTekstBuilder tekstBuilder,
                                     VurdertForeldelsePeriode forrigeForeldelsePeriode) {
        boolean harEndret = false;
        if (!foreldelsePeriode.getForeldelseVurderingType().equals(forrigeForeldelsePeriode.getForeldelseVurderingType())) {
            harEndret = true;
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSE,
                    forrigeForeldelsePeriode.getForeldelseVurderingType().getNavn(),
                    foreldelsePeriode.getForeldelseVurderingType().getNavn());
            // hvis saksbehandler endret vurdering type, må vi starte vilkårs på nytt
            if (ForeldelseVurderingType.FORELDET.equals(foreldelsePeriode.getForeldelseVurderingType())) {
                sletteVilkårData(behandlingId);
            }
        }
        if ((ForeldelseVurderingType.FORELDET.equals(foreldelsePeriode.getForeldelseVurderingType()) || ForeldelseVurderingType.TILLEGGSFRIST.equals(foreldelsePeriode.getForeldelseVurderingType()))
                && ((foreldelsePeriode.getForeldelsesfrist() != null && !foreldelsePeriode.getForeldelsesfrist().equals(forrigeForeldelsePeriode.getForeldelsesfrist()))
                || (forrigeForeldelsePeriode.getForeldelsesfrist() != null && !forrigeForeldelsePeriode.getForeldelsesfrist().equals(foreldelsePeriode.getForeldelsesfrist())))
        ) {
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSESFRIST, forrigeForeldelsePeriode.getForeldelsesfrist(), foreldelsePeriode.getForeldelsesfrist());
            harEndret = true;
        }
        if (ForeldelseVurderingType.TILLEGGSFRIST.equals(foreldelsePeriode.getForeldelseVurderingType())
                && (foreldelsePeriode.getOppdagelsesDato() != null && !foreldelsePeriode.getOppdagelsesDato().equals(forrigeForeldelsePeriode.getOppdagelsesDato()))
                || (forrigeForeldelsePeriode.getOppdagelsesDato() != null && !forrigeForeldelsePeriode.getOppdagelsesDato().equals(foreldelsePeriode.getOppdagelsesDato()))
        ) {
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.OPPDAGELSES_DATO, forrigeForeldelsePeriode.getOppdagelsesDato(), foreldelsePeriode.getOppdagelsesDato());
            harEndret = true;
        }
        return harEndret;
    }

    private void lagNyttInnslag(VurdertForeldelsePeriode foreldelsePeriode, HistorikkInnslagTekstBuilder tekstBuilder) {
        tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSE, null, foreldelsePeriode.getForeldelseVurderingType().getNavn());
        if (foreldelsePeriode.getForeldelsesfrist() != null) {
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSESFRIST, null, foreldelsePeriode.getForeldelsesfrist());
        }
        if (foreldelsePeriode.getOppdagelsesDato() != null) {
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.OPPDAGELSES_DATO, null, foreldelsePeriode.getOppdagelsesDato());
        }
    }

    private void sletteVilkårData(Long behandlingId) {
        vilkårsvurderingRepository.slettVilkårsvurdering(behandlingId);
    }

}
