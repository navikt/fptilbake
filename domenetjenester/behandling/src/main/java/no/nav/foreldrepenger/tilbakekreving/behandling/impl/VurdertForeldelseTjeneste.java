package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.PeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingAggregate;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingPeriodeÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseAggregate;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.vedtak.felles.jpa.Transaction;

@ApplicationScoped
@Transaction
public class VurdertForeldelseTjeneste {

    private VurdertForeldelseRepository vurdertForeldelseRepository;
    private BehandlingRepositoryProvider repositoryProvider;
    private KravgrunnlagRepository grunnlagRepository;
    private FeilutbetalingRepository feilutbetalingRepository;
    private HistorikkTjenesteAdapter historikkTjenesteAdapter;
    private VilkårsvurderingRepository vilkårsvurderingRepository;

    VurdertForeldelseTjeneste() {
        // For CDI
    }

    @Inject
    public VurdertForeldelseTjeneste(VurdertForeldelseRepository vurdertForeldelseRepository, BehandlingRepositoryProvider repositoryProvider,
                                     FeilutbetalingRepository feilutbetalingRepository, HistorikkTjenesteAdapter historikkTjenesteAdapter) {
        this.vurdertForeldelseRepository = vurdertForeldelseRepository;
        this.repositoryProvider = repositoryProvider;
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.feilutbetalingRepository = feilutbetalingRepository;
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
    }

    public void lagreVurdertForeldelseGrunnlag(Long behandlingId, List<ForeldelsePeriodeDto> foreldelsePerioder) {
        VurdertForeldelse vurdertForeldelse = new VurdertForeldelse();
        for (ForeldelsePeriodeDto foreldelsePeriodeDto : foreldelsePerioder) {
            VurdertForeldelsePeriode vurdertForeldelsePeriode = lagVurdertForeldelse(vurdertForeldelse, foreldelsePeriodeDto);
            vurdertForeldelse.leggTilVurderForeldelsePerioder(vurdertForeldelsePeriode);
        }
        VurdertForeldelseAggregate vurdertForeldelseAggregate = VurdertForeldelseAggregate.builder()
            .medVurdertForeldelse(vurdertForeldelse)
            .medAktiv(true)
            .medBehandlingId(behandlingId).build();

        Optional<VurdertForeldelseAggregate> forrigeVurdertForeldelseAggregate = vurdertForeldelseRepository.finnVurdertForeldelseForBehandling(behandlingId);
        vurdertForeldelseRepository.lagre(vurdertForeldelseAggregate);
        lagInnslag(behandlingId, forrigeVurdertForeldelseAggregate, vurdertForeldelseAggregate);

    }

    public FeilutbetalingPerioderDto hentFaktaPerioder(Long behandlingId) {
        Optional<FeilutbetalingAggregate> feilutbetalingAggregate = feilutbetalingRepository.finnFeilutbetaling(behandlingId);
        FeilutbetalingPerioderDto perioderDto = new FeilutbetalingPerioderDto();
        List<PeriodeDto> perioder = new ArrayList<>();
        if (feilutbetalingAggregate.isPresent() && !feilutbetalingAggregate.get().getFeilutbetaling().getFeilutbetaltPerioder().isEmpty()) {
            for (FeilutbetalingPeriodeÅrsak feilutbetalingPeriodeÅrsak : feilutbetalingAggregate.get().getFeilutbetaling().getFeilutbetaltPerioder()) {
                PeriodeDto periode = new PeriodeDto();
                periode.setPeriode(feilutbetalingPeriodeÅrsak.getPeriode());
                periode.setForeldelseVurderingType(ForeldelseVurderingType.UDEFINERT);
                perioder.add(periode);
            }

            List<Periode> råPerioder = perioder.stream().map(PeriodeDto::tilPeriode).collect(Collectors.toList());
            Map<Periode, BigDecimal> feilPrPeriode = beregnFeilutbetaltBeløpForPerioder(behandlingId, råPerioder);

            for (PeriodeDto periodeDto : perioder) {
                periodeDto.setBelop(feilPrPeriode.get(periodeDto.tilPeriode()));
            }

            perioderDto.setPerioder(perioder);
        }
        return perioderDto;
    }

    public FeilutbetalingPerioderDto henteVurdertForeldelse(Long behandlingId) {
        Optional<VurdertForeldelseAggregate> vurdertForeldelseGrunnlag = vurdertForeldelseRepository.finnVurdertForeldelseForBehandling(behandlingId);
        FeilutbetalingPerioderDto feilutbetalingPerioderDto = new FeilutbetalingPerioderDto();
        if (vurdertForeldelseGrunnlag.isPresent()) {
            KravgrunnlagAggregate aggregate = grunnlagRepository.finnEksaktGrunnlagForBehandlingId(behandlingId);
            List<PeriodeDto> perioder = new ArrayList<>();
            VurdertForeldelseAggregate vurdertForeldelseAggregate = vurdertForeldelseGrunnlag.get();
            VurdertForeldelse vurdertForeldelse = vurdertForeldelseAggregate.getVurdertForeldelse();
            List<VurdertForeldelsePeriode> foreldelsePerioder = new ArrayList<>(vurdertForeldelse.getVurdertForeldelsePerioder());
            foreldelsePerioder.sort(Comparator.comparing(VurdertForeldelsePeriode::getFom));
            for (VurdertForeldelsePeriode vurdertForeldelsePeriode : foreldelsePerioder) {
                PeriodeDto periodeDto = new PeriodeDto();
                periodeDto.setPeriode(vurdertForeldelsePeriode.getPeriode());
                periodeDto.setForeldelseVurderingType(vurdertForeldelsePeriode.getForeldelseVurderingType());
                periodeDto.setBegrunnelse(vurdertForeldelsePeriode.getBegrunnelse());
                periodeDto.setBelop(beregnFeilutbetaltBeløp(aggregate, vurdertForeldelsePeriode.getPeriode()));
                perioder.add(periodeDto);
            }
            feilutbetalingPerioderDto.setPerioder(perioder);
        }
        return feilutbetalingPerioderDto;
    }

    public Map<Periode, BigDecimal> beregnFeilutbetaltBeløpForPerioder(Long behandlingId, List<Periode> perioder) {
        KravgrunnlagAggregate aggregate = grunnlagRepository.finnEksaktGrunnlagForBehandlingId(behandlingId);

        var map = new HashMap<Periode, BigDecimal>();
        for (Periode periode : perioder) {
            map.put(periode, beregnFeilutbetaltBeløp(aggregate, periode));
        }
        return map;
    }

    public boolean harForeldetPeriodeForBehandlingId(Long behandlingId) {
        return vurdertForeldelseRepository.harVurdertForeldelseForBehandlingId(behandlingId);
    }

    private VurdertForeldelsePeriode lagVurdertForeldelse(VurdertForeldelse vurdertForeldelse, ForeldelsePeriodeDto foreldelsePeriodeDto) {
        return VurdertForeldelsePeriode.builder().medVurdertForeldelse(vurdertForeldelse)
            .medForeldelseVurderingType(foreldelsePeriodeDto.getForeldelseVurderingType())
            .medPeriode(foreldelsePeriodeDto.getFraDato(), foreldelsePeriodeDto.getTilDato())
            .medBegrunnelse(foreldelsePeriodeDto.getBegrunnelse())
            .build();
    }

    private BigDecimal beregnFeilutbetaltBeløp(KravgrunnlagAggregate aggregate, Periode foreldetPeriode) {
        Kravgrunnlag431 kravgrunnlag = aggregate.getGrunnlagØkonomi();
        List<KravgrunnlagPeriode432> kgPerioder = new ArrayList<>(kravgrunnlag.getPerioder());
        kgPerioder.sort(Comparator.comparing(p -> p.getPeriode().getFom()));
        BigDecimal sum = BigDecimal.ZERO;
        for (KravgrunnlagPeriode432 kgPeriode : kgPerioder) {
            BigDecimal feilutbetaltBeløp = kgPeriode.getKravgrunnlagBeloper433().stream()
                .filter(kgBeløp -> kgBeløp.getKlasseType().equals(KlasseType.FEIL))
                .map(KravgrunnlagBelop433::getNyBelop)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (isNotZero(feilutbetaltBeløp)) {
                BigDecimal feilutbetaltBeløpPrVirkedag = BeregnBeløpUtil.beregnBeløpPrVirkedag(feilutbetaltBeløp, kgPeriode.getPeriode());
                sum = sum.add(BeregnBeløpUtil.beregnBeløp(foreldetPeriode, kgPeriode.getPeriode(), feilutbetaltBeløpPrVirkedag));
            }
        }

        return sum.setScale(0, RoundingMode.HALF_UP);
    }

    private static boolean isNotZero(BigDecimal verdi) {
        return verdi.signum() != 0;
    }

    private void lagInnslag(Long behandlingId, Optional<VurdertForeldelseAggregate> forrigeVurdertForeldelse, VurdertForeldelseAggregate vurdertForeldelseAggregate) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FORELDELSE);
        historikkinnslag.setBehandlingId(behandlingId);
        historikkinnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);

        boolean behovForHistorikkInnslag = false;
        for (VurdertForeldelsePeriode foreldelsePeriode : vurdertForeldelseAggregate.getVurdertForeldelse().getVurdertForeldelsePerioder()) {
            HistorikkInnslagTekstBuilder tekstBuilder = historikkTjenesteAdapter.tekstBuilder();
            boolean harEndret = false;
            // forrigeVurdertForeldelse finnes ikke
            if (forrigeVurdertForeldelse.isEmpty()) {
                harEndret = true;
                tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSE, null, finnForeldelseVurderingType(foreldelsePeriode.getForeldelseVurderingType()).getNavn());
            } else {
                harEndret = opprettInnslagNårForrigePerioderFinnes(forrigeVurdertForeldelse, foreldelsePeriode, tekstBuilder);
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

    private boolean opprettInnslagNårForrigePerioderFinnes(Optional<VurdertForeldelseAggregate> forrigeVurdertForeldelse, VurdertForeldelsePeriode foreldelsePeriode,
                                                           HistorikkInnslagTekstBuilder tekstBuilder) {
        VurdertForeldelseAggregate forrigeVurdertForeldelseAggregate = forrigeVurdertForeldelse.get();
        Optional<VurdertForeldelsePeriode> forrigeForeldelsePeriode = forrigeVurdertForeldelseAggregate.getVurdertForeldelse().getVurdertForeldelsePerioder()
            .stream()
            .filter(vurdertForeldelsePeriode -> vurdertForeldelsePeriode.getPeriode().equals(foreldelsePeriode.getPeriode()))
            .findAny();
        boolean harEndret = false;
        // samme perioder med endret foreldelse vurdering type
        if (!forrigeForeldelsePeriode.isEmpty() &&
            !foreldelsePeriode.getForeldelseVurderingType().equals(forrigeForeldelsePeriode.get().getForeldelseVurderingType())) {
            harEndret = true;
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSE,
                finnForeldelseVurderingType(forrigeForeldelsePeriode.get().getForeldelseVurderingType()).getNavn(),
                finnForeldelseVurderingType(foreldelsePeriode.getForeldelseVurderingType()).getNavn());
            // hvis saksbehandler endret vurdering type, må vi starte vilkårs på nytt
            if (ForeldelseVurderingType.FORELDET.equals(foreldelsePeriode.getForeldelseVurderingType())) {
                sletteVilkårData(forrigeVurdertForeldelse.get().getBehandlingId());
            }

        } else if (forrigeForeldelsePeriode.isEmpty()) { // nye perioder
            harEndret = true;
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FORELDELSE, null, finnForeldelseVurderingType(foreldelsePeriode.getForeldelseVurderingType()).getNavn());
            // hvis saksbehandler deler opp perioder, må vi starte vilkårs på nytt
            sletteVilkårData(forrigeVurdertForeldelse.get().getBehandlingId());
        } else if (!forrigeForeldelsePeriode.isEmpty() && !foreldelsePeriode.getBegrunnelse().equals(forrigeForeldelsePeriode.get().getBegrunnelse())) {
            harEndret = true;
        }
        return harEndret;
    }


    private void sletteVilkårData(Long behandlingId) {
        vilkårsvurderingRepository.sletteVilkårsvurdering(behandlingId);
    }

    private ForeldelseVurderingType finnForeldelseVurderingType(ForeldelseVurderingType foreldelseVurderingType) {
        return repositoryProvider.getKodeverkRepository().finn(ForeldelseVurderingType.class, foreldelseVurderingType);
    }

}
