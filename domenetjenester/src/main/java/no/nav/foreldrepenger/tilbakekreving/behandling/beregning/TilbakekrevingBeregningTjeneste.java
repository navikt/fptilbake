package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BeregnBeløpUtil;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FordeltKravgrunnlagBeløp;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.SaksbehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

@ApplicationScoped
public class TilbakekrevingBeregningTjeneste {

    private KravgrunnlagRepository kravgrunnlagRepository;
    private VurdertForeldelseRepository vurdertForeldelseRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BehandlingRepository behandlingRepository;

    private KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste;


    TilbakekrevingBeregningTjeneste() {
        //for CDI proxy
    }

    @Inject
    public TilbakekrevingBeregningTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                           KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste) {
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.vurdertForeldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.kravgrunnlagBeregningTjeneste = kravgrunnlagBeregningTjeneste;
    }

    BeregningResultat beregn(Long behandlingId) {
        var kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        var vurdertForeldelse = hentVurdertForeldelse(behandlingId);
        var vilkårsvurdering = hentVilkårsvurdering(behandlingId);
        var perioder = finnPerioder(vurdertForeldelse, vilkårsvurdering);
        var perioderMedBeløp = kravgrunnlagBeregningTjeneste.fordelKravgrunnlagBeløpPåPerioder(kravgrunnlag, perioder);

        var beregningResultatPerioder = beregn(kravgrunnlag, vurdertForeldelse, vilkårsvurdering, perioderMedBeløp, skalBeregneRenter(behandling));
        var totalTilbakekrevingBeløp = sum(beregningResultatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløp);
        var totalFeilutbetaltBeløp = sum(beregningResultatPerioder, BeregningResultatPeriode::getFeilutbetaltBeløp);

        var vedtakResultatType = bestemVedtakResultat(behandlingId, totalTilbakekrevingBeløp, totalFeilutbetaltBeløp);
        return new BeregningResultat(vedtakResultatType, beregningResultatPerioder);
    }

    private boolean skalBeregneRenter(Behandling behandling) {
        return !FagsakYtelseType.FRISINN.equals(behandling.getFagsak().getFagsakYtelseType());
    }

    private VilkårVurderingEntitet hentVilkårsvurdering(Long behandlingId) {
        return vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId).orElse(new VilkårVurderingEntitet());
    }

    private VurdertForeldelse hentVurdertForeldelse(Long behandlingId) {
        return vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId).orElse(new VurdertForeldelse());
    }

    private List<Periode> finnPerioder(VurdertForeldelse vurdertForeldelse, VilkårVurderingEntitet vilkårsvurdering) {
        var foreldedePerioder = finnForeldedePerioder(vurdertForeldelse);
        var ikkeForeldedePerioder = finnIkkeForeldedePerioder(vilkårsvurdering);

        return tilEnListe(foreldedePerioder, ikkeForeldedePerioder);
    }

    private List<BeregningResultatPeriode> beregn(Kravgrunnlag431 kravgrunnlag,
                                                  VurdertForeldelse vurdertForeldelse,
                                                  VilkårVurderingEntitet vilkårsvurdering,
                                                  Map<Periode, FordeltKravgrunnlagBeløp> perioderMedBeløp,
                                                  boolean beregnRenter) {
        List<BeregningResultatPeriode> resulat = new ArrayList<>();
        resulat.addAll(beregnForForeldedePerioder(vurdertForeldelse, perioderMedBeløp));
        resulat.addAll(beregnForIkkeForeldedePerioder(kravgrunnlag, vilkårsvurdering, perioderMedBeløp, beregnRenter));
        resulat.sort(Comparator.comparing(b -> b.getPeriode().getFom()));
        return resulat;
    }

    private List<Periode> finnIkkeForeldedePerioder(VilkårVurderingEntitet vilkårsvurdering) {
        return vilkårsvurdering.getPerioder().stream().map(VilkårVurderingPeriodeEntitet::getPeriode).toList();
    }

    private List<Periode> finnForeldedePerioder(VurdertForeldelse vurdertForeldelse) {
        return vurdertForeldelse.getVurdertForeldelsePerioder()
            .stream()
            .filter(VurdertForeldelsePeriode::erForeldet)
            .map(VurdertForeldelsePeriode::getPeriode)
            .toList();
    }

    private static List<Periode> tilEnListe(List<Periode> list1, List<Periode> list2) {
        List<Periode> resultat = new ArrayList<>(list1);
        resultat.addAll(list2);
        return resultat;
    }

    private Collection<BeregningResultatPeriode> beregnForIkkeForeldedePerioder(Kravgrunnlag431 kravgrunnlag,
                                                                                VilkårVurderingEntitet vilkårsvurdering,
                                                                                Map<Periode, FordeltKravgrunnlagBeløp> kravbeløpPrPeriode,
                                                                                boolean beregnRenter) {
        return vilkårsvurdering.getPerioder()
            .stream()
            .map(p -> beregnIkkeForeldetPeriode(kravgrunnlag, p, kravbeløpPrPeriode, beregnRenter))
            .toList();
    }

    private Collection<BeregningResultatPeriode> beregnForForeldedePerioder(VurdertForeldelse vurdertForeldelse,
                                                                            Map<Periode, FordeltKravgrunnlagBeløp> kravbeløpPrPeriode) {
        return vurdertForeldelse.getVurdertForeldelsePerioder()
            .stream()
            .filter(p -> ForeldelseVurderingType.FORELDET.equals(p.getForeldelseVurderingType()))
            .map(p -> beregnForeldetPeriode(kravbeløpPrPeriode, p))
            .toList();
    }

    private BeregningResultatPeriode beregnForeldetPeriode(Map<Periode, FordeltKravgrunnlagBeløp> beløpPerPeriode,
                                                           VurdertForeldelsePeriode foreldelsePeriode) {
        var periode = foreldelsePeriode.getPeriode();
        var delresultat = beløpPerPeriode.get(periode);
        return BeregningResultatPeriode.builder()
            .medPeriode(periode)
            .medFeilutbetaltBeløp(delresultat.getFeilutbetaltBeløp())
            .medRiktigYtelseBeløp(delresultat.getRiktigYtelseBeløp())
            .medUtbetaltYtelseBeløp(delresultat.getUtbetaltYtelseBeløp())
            .medTilbakekrevingBeløp(BigDecimal.ZERO)
            .medTilbakekrevingBeløpUtenRenter(BigDecimal.ZERO)
            .medRenteBeløp(BigDecimal.ZERO)
            .medSkattBeløp(BigDecimal.ZERO)
            .medTilbakekrevingBeløpEtterSkatt(BigDecimal.ZERO)
            .build();
    }

    private BeregningResultatPeriode beregnIkkeForeldetPeriode(Kravgrunnlag431 kravgrunnlag,
                                                               VilkårVurderingPeriodeEntitet vurdering,
                                                               Map<Periode, FordeltKravgrunnlagBeløp> kravbeløpPrPeriode,
                                                               boolean beregnRenter) {
        var periode = vurdering.getPeriode();
        var delresultat = kravbeløpPrPeriode.get(periode);
        var perioderMedSkattProsent = lagGrunnlagPeriodeMedSkattProsent(periode, kravgrunnlag);
        return TilbakekrevingBeregnerVilkår.beregn(vurdering, delresultat, perioderMedSkattProsent, beregnRenter);
    }

    private List<GrunnlagPeriodeMedSkattProsent> lagGrunnlagPeriodeMedSkattProsent(Periode periode, Kravgrunnlag431 kravgrunnlag) {
        var beregnBeløpUtil = BeregnBeløpUtil.forFagområde(kravgrunnlag.getFagOmrådeKode());
        List<KravgrunnlagPeriode432> kgPerioder = new ArrayList<>(kravgrunnlag.getPerioder());
        kgPerioder.sort(Comparator.comparing(p -> p.getPeriode().getFom()));

        List<GrunnlagPeriodeMedSkattProsent> perioderMedSkattProsent = new ArrayList<>();
        for (var kgPeriode : kgPerioder) {
            var kgBeløper = kgPeriode.getKravgrunnlagBeloper433()
                .stream()
                .filter(kgBeløp -> KlasseType.YTEL.equals(kgBeløp.getKlasseType()))
                .toList();
            var periodeMedSkattProsent = kgBeløper.stream().map(kgBeløp -> {
                var maksTilbakekrevesBeløp = beregnBeløpUtil.beregnBeløpForPeriode(kgBeløp.getTilbakekrevesBelop(), periode, kgPeriode.getPeriode());
                return new GrunnlagPeriodeMedSkattProsent(kgPeriode.getPeriode(), maksTilbakekrevesBeløp, kgBeløp.getSkattProsent());
            }).toList();
            perioderMedSkattProsent.addAll(periodeMedSkattProsent);
        }
        return perioderMedSkattProsent;
    }

    private VedtakResultatType bestemVedtakResultat(long behandlingId, BigDecimal tilbakekrevingBeløp, BigDecimal feilutbetaltBeløp) {
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        if (SaksbehandlingType.AUTOMATISK_IKKE_INNKREVING_LAVT_BELØP.equals(behandling.getSaksbehandlingType())) {
            return VedtakResultatType.INGEN_TILBAKEBETALING;
        }
        if (tilbakekrevingBeløp.compareTo(BigDecimal.ZERO) == 0) {
            return VedtakResultatType.INGEN_TILBAKEBETALING;
        } else if (tilbakekrevingBeløp.compareTo(feilutbetaltBeløp) < 0) {
            return VedtakResultatType.DELVIS_TILBAKEBETALING;
        }
        return VedtakResultatType.FULL_TILBAKEBETALING;
    }

    private static <T> BigDecimal sum(Collection<T> liste, Function<T, BigDecimal> konverter) {
        return liste.stream().map(konverter).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
