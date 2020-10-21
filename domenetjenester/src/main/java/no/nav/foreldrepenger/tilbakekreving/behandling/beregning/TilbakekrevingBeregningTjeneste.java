package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BeregnBeløpUtil;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FordeltKravgrunnlagBeløp;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
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
    public TilbakekrevingBeregningTjeneste(BehandlingRepositoryProvider repositoryProvider, KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste) {
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.vurdertForeldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.kravgrunnlagBeregningTjeneste = kravgrunnlagBeregningTjeneste;
    }

    public BeregningResultat beregn(Long behandlingId) {
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VurdertForeldelse vurdertForeldelse = hentVurdertForeldelse(behandlingId);
        VilkårVurderingEntitet vilkårsvurdering = hentVilkårsvurdering(behandlingId);
        List<Periode> perioder = finnPerioder(vurdertForeldelse, vilkårsvurdering);
        Map<Periode, FordeltKravgrunnlagBeløp> perioderMedBeløp = kravgrunnlagBeregningTjeneste.fordelKravgrunnlagBeløpPåPerioder(kravgrunnlag, perioder);

        List<BeregningResultatPeriode> beregningResultatPerioder = beregn(kravgrunnlag, vurdertForeldelse, vilkårsvurdering, perioderMedBeløp, skalBeregneRenter(behandling));
        BigDecimal totalTilbakekrevingBeløp = sum(beregningResultatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløp);
        BigDecimal totalFeilutbetaltBeløp = sum(beregningResultatPerioder, BeregningResultatPeriode::getFeilutbetaltBeløp);

        BeregningResultat beregningResultat = new BeregningResultat();
        beregningResultat.setVedtakResultatType(bestemVedtakResultat(behandlingId,totalTilbakekrevingBeløp, totalFeilutbetaltBeløp));
        beregningResultat.setBeregningResultatPerioder(beregningResultatPerioder);
        return beregningResultat;
    }

    private boolean skalBeregneRenter(Behandling behandling) {
        return !FagsakYtelseType.FRISINN.equals(behandling.getFagsak().getFagsakYtelseType());
    }

    private VilkårVurderingEntitet hentVilkårsvurdering(Long behandlingId) {
        VilkårVurderingEntitet vurderingUtenPerioder = new VilkårVurderingEntitet();
        return vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId)
            .orElse(vurderingUtenPerioder);
    }

    private VurdertForeldelse hentVurdertForeldelse(Long behandlingId) {
        VurdertForeldelse vurderingUtenPerioder = new VurdertForeldelse();
        return vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId).orElse(vurderingUtenPerioder);
    }

    private List<Periode> finnPerioder(VurdertForeldelse vurdertForeldelse, VilkårVurderingEntitet vilkårsvurdering) {
        List<Periode> foreldedePerioder = finnForeldedePerioder(vurdertForeldelse);
        List<Periode> ikkeForeldedePerioder = finnIkkeForeldedePerioder(vilkårsvurdering);

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
        return vilkårsvurdering.getPerioder()
            .stream()
            .map(VilkårVurderingPeriodeEntitet::getPeriode)
            .collect(Collectors.toList());
    }

    private List<Periode> finnForeldedePerioder(VurdertForeldelse vurdertForeldelse) {
        return vurdertForeldelse.getVurdertForeldelsePerioder()
            .stream()
            .filter(VurdertForeldelsePeriode::erForeldet)
            .map(VurdertForeldelsePeriode::getPeriode)
            .collect(Collectors.toList());
    }

    private static <T> List<T> tilEnListe(List<T>... lister) {
        List<T> resultat = new ArrayList<>();
        for (List<T> liste : lister) {
            resultat.addAll(liste);
        }
        return resultat;
    }

    private Collection<BeregningResultatPeriode> beregnForIkkeForeldedePerioder(Kravgrunnlag431 kravgrunnlag,
                                                                                VilkårVurderingEntitet vilkårsvurdering,
                                                                                Map<Periode, FordeltKravgrunnlagBeløp> kravbeløpPrPeriode,
                                                                                boolean beregnRenter) {
        return vilkårsvurdering.getPerioder()
            .stream()
            .map(p -> beregnIkkeForeldetPeriode(kravgrunnlag, p, kravbeløpPrPeriode, beregnRenter))
            .collect(Collectors.toList());
    }

    private Collection<BeregningResultatPeriode> beregnForForeldedePerioder(VurdertForeldelse vurdertForeldelse, Map<Periode, FordeltKravgrunnlagBeløp> kravbeløpPrPeriode) {
        return vurdertForeldelse.getVurdertForeldelsePerioder()
            .stream()
            .filter(p -> ForeldelseVurderingType.FORELDET.equals(p.getForeldelseVurderingType()))
            .map(p -> beregnForeldetPeriode(kravbeløpPrPeriode, p))
            .collect(Collectors.toList());
    }

    private BeregningResultatPeriode beregnForeldetPeriode(Map<Periode, FordeltKravgrunnlagBeløp> beløpPerPeriode, VurdertForeldelsePeriode foreldelsePeriode) {
        Periode periode = foreldelsePeriode.getPeriode();
        BeregningResultatPeriode resultat = new BeregningResultatPeriode();
        resultat.setPeriode(periode);
        FordeltKravgrunnlagBeløp delresultat = beløpPerPeriode.get(periode);
        resultat.setFeilutbetaltBeløp(delresultat.getFeilutbetaltBeløp());
        resultat.setRiktigYtelseBeløp(delresultat.getRiktigYtelseBeløp());
        resultat.setUtbetaltYtelseBeløp(delresultat.getUtbetaltYtelseBeløp());
        resultat.setTilbakekrevingBeløp(BigDecimal.ZERO);
        resultat.setTilbakekrevingBeløpUtenRenter(BigDecimal.ZERO);
        resultat.setRenteBeløp(BigDecimal.ZERO);
        resultat.setAndelAvBeløp(BigDecimal.ZERO);
        resultat.setVurdering(AnnenVurdering.FORELDET);
        resultat.setSkattBeløp(BigDecimal.ZERO);
        resultat.setTilbakekrevingBeløpEtterSkatt(BigDecimal.ZERO);
        return resultat;
    }

    private BeregningResultatPeriode beregnIkkeForeldetPeriode(Kravgrunnlag431 kravgrunnlag,
                                                               VilkårVurderingPeriodeEntitet vurdering,
                                                               Map<Periode, FordeltKravgrunnlagBeløp> kravbeløpPrPeriode,
                                                               boolean beregnRenter) {
        Periode periode = vurdering.getPeriode();
        FordeltKravgrunnlagBeløp delresultat = kravbeløpPrPeriode.get(periode);
        List<GrunnlagPeriodeMedSkattProsent> perioderMedSkattProsent = lagGrunnlagPeriodeMedSkattProsent(periode, kravgrunnlag);
        return TilbakekrevingBeregnerVilkår.beregn(vurdering, delresultat, perioderMedSkattProsent, beregnRenter);
    }

    private List<GrunnlagPeriodeMedSkattProsent> lagGrunnlagPeriodeMedSkattProsent(Periode periode, Kravgrunnlag431 kravgrunnlag) {
        BeregnBeløpUtil beregnBeløpUtil = BeregnBeløpUtil.forFagområde(kravgrunnlag.getFagOmrådeKode());
        List<KravgrunnlagPeriode432> kgPerioder = new ArrayList<>(kravgrunnlag.getPerioder());
        kgPerioder.sort(Comparator.comparing(p -> p.getPeriode().getFom()));

        List<GrunnlagPeriodeMedSkattProsent> perioderMedSkattProsent = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : kgPerioder) {
            List<KravgrunnlagBelop433> kgBeløper = kgPeriode.getKravgrunnlagBeloper433().stream()
                .filter(kgBeløp -> KlasseType.YTEL.equals(kgBeløp.getKlasseType()))
                .collect(Collectors.toList());
            List<GrunnlagPeriodeMedSkattProsent> periodeMedSkattProsent = kgBeløper.stream()
                .map(kgBeløp -> {
                    BigDecimal maksTilbakekrevesBeløp = beregnBeløpUtil.beregnBeløpForPeriode(kgBeløp.getTilbakekrevesBelop(), periode, kgPeriode.getPeriode());
                    return new GrunnlagPeriodeMedSkattProsent(kgPeriode.getPeriode(), maksTilbakekrevesBeløp, kgBeløp.getSkattProsent());
                })
                .collect(Collectors.toList());
            perioderMedSkattProsent.addAll(periodeMedSkattProsent);
        }
        return perioderMedSkattProsent;
    }

    private VedtakResultatType bestemVedtakResultat(long behandlingId, BigDecimal tilbakekrevingBeløp, BigDecimal feilutbetaltBeløp) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
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
        return liste.stream()
            .map(konverter)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
