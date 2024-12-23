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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagEndretEvent;

@ApplicationScoped
@Transactional
public class VurdertForeldelseTjeneste {

    private VurdertForeldelseRepository vurdertForeldelseRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private BehandlingRepository behandlingRepository;

    private VurderForeldelseHistorikkTjeneste vurderForeldelseHistorikkTjeneste;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste;

    VurdertForeldelseTjeneste() {
        // For CDI
    }

    @Inject
    public VurdertForeldelseTjeneste(BehandlingRepositoryProvider repositoryProvider, VurderForeldelseHistorikkTjeneste vurderForeldelseHistorikkTjeneste, KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste) {
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.vurdertForeldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.faktaFeilutbetalingRepository = repositoryProvider.getFaktaFeilutbetalingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();

        this.vurderForeldelseHistorikkTjeneste = vurderForeldelseHistorikkTjeneste;
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
        if (skalVilkårDataSlettes(forrigeVurdertForeldelse, vurdertForeldelse)) {
            vilkårsvurderingRepository.slettVilkårsvurdering(behandlingId);
        }

        vurderForeldelseHistorikkTjeneste.lagHistorikkinnslagForeldelse(behandlingRepository.hentBehandling(behandlingId), forrigeVurdertForeldelse, vurdertForeldelse);
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

    private static boolean skalVilkårDataSlettes(Optional<VurdertForeldelse> forrigeVurdertForeldelseOpt, VurdertForeldelse vurdertForeldelseAggregate) {
        if (forrigeVurdertForeldelseOpt.isEmpty()) {
            return false;
        }

        var forrigeVurdertForeldelse = forrigeVurdertForeldelseOpt.get();
        for (var foreldelsePeriode : vurdertForeldelseAggregate.getVurdertForeldelsePerioder()) {
            var forrigeForeldelsePeriode = forrigeForeldelsePeriode(forrigeVurdertForeldelse, foreldelsePeriode);
            if (forrigeForeldelsePeriode.isEmpty()) {
                return true; // hvis saksbehandler deler opp perioder, må vi starte vilkårs på nytt
            }

            // hvis saksbehandler endret vurdering type, må vi starte vilkårs på nytt
            var vurdertForeldelsePeriode = forrigeForeldelsePeriode.get();
            if (ForeldelseVurderingType.FORELDET.equals(foreldelsePeriode.getForeldelseVurderingType()) &&
                !foreldelsePeriode.getForeldelseVurderingType().equals(vurdertForeldelsePeriode.getForeldelseVurderingType())) {
                return true;
            }
        }
        return false;
    }

    protected static Optional<VurdertForeldelsePeriode> forrigeForeldelsePeriode(VurdertForeldelse forrigeVurdertForeldelse, VurdertForeldelsePeriode foreldelsePeriode) {
        return forrigeVurdertForeldelse.getVurdertForeldelsePerioder()
            .stream()
            .filter(vurdertForeldelsePeriode -> vurdertForeldelsePeriode.getPeriode().equals(foreldelsePeriode.getPeriode()))
            .findAny();
    }
}
