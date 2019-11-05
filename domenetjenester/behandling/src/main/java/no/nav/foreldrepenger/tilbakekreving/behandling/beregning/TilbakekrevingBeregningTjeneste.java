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
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseAggregate;
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

    private VurdertForeldelseTjeneste vurdertForeldelseTjeneste;
    private KravgrunnlagRepository kravgrunnlagRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private VurdertForeldelseRepository vurdertForeldelseRepository;

    TilbakekrevingBeregningTjeneste() {
        //for CDI proxy
    }

    @Inject
    public TilbakekrevingBeregningTjeneste(VurdertForeldelseTjeneste vurdertForeldelseTjeneste, BehandlingRepositoryProvider repositoryProvider) {
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.vurdertForeldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
    }

    public BeregningResultat beregn(Long behandlingId) {
        Kravgrunnlag431 kravgrunnlag = hentKravgrunnlag(behandlingId);
        VurdertForeldelse vurdertForeldelse = hentVurdertForeldelse(behandlingId);
        VilkårVurderingEntitet vilkårsvurdering = hentVilkårsvurdering(behandlingId);
        List<Periode> perioder = finnPerioder(vurdertForeldelse, vilkårsvurdering);
        Map<Periode, BigDecimal> perioderMedBeløp = vurdertForeldelseTjeneste.beregnFeilutbetaltBeløpForPerioder(kravgrunnlag, perioder);

        List<BeregningResultatPeriode> beregningResultatPerioder = beregn(kravgrunnlag, vurdertForeldelse, vilkårsvurdering, perioderMedBeløp);
        sjekkOgJusterTotalSkattBeløp(kravgrunnlag.getPerioder(), beregningResultatPerioder);
        BigDecimal totalTilbakekrevingBeløp = sum(beregningResultatPerioder, BeregningResultatPeriode::getTilbakekrevingBeløp);
        BigDecimal totalFeilutbetaltBeløp = sum(beregningResultatPerioder, BeregningResultatPeriode::getFeilutbetaltBeløp);

        BeregningResultat beregningResultat = new BeregningResultat();
        beregningResultat.setVedtakResultatType(bestemVedtakResultat(totalTilbakekrevingBeløp, totalFeilutbetaltBeløp));
        beregningResultat.setBeregningResultatPerioder(beregningResultatPerioder);
        return beregningResultat;
    }

    private VilkårVurderingEntitet hentVilkårsvurdering(Long behandlingId) {
        VilkårVurderingEntitet vurderingUtenPerioder = new VilkårVurderingEntitet();
        return vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId)
            .orElse(vurderingUtenPerioder);
    }

    private VurdertForeldelse hentVurdertForeldelse(Long behandlingId) {
        VurdertForeldelse vurderingUtenPerioder = new VurdertForeldelse();
        return vurdertForeldelseRepository.finnVurdertForeldelseForBehandling(behandlingId)
            .map(VurdertForeldelseAggregate::getVurdertForeldelse)
            .orElse(vurderingUtenPerioder);
    }

    private Kravgrunnlag431 hentKravgrunnlag(Long behandlingId) {
        return kravgrunnlagRepository
            .finnGrunnlagForBehandlingId(behandlingId)
            .orElseThrow()
            .getGrunnlagØkonomi();
    }

    private List<Periode> finnPerioder(VurdertForeldelse vurdertForeldelse, VilkårVurderingEntitet vilkårsvurdering) {
        List<Periode> foreldedePerioder = finnForeldedePerioder(vurdertForeldelse);
        List<Periode> ikkeForeldedePerioder = finnIkkeForeldedePerioder(vilkårsvurdering);

        return tilEnListe(foreldedePerioder, ikkeForeldedePerioder);
    }

    private List<BeregningResultatPeriode> beregn(Kravgrunnlag431 kravgrunnlag, VurdertForeldelse vurdertForeldelse, VilkårVurderingEntitet vilkårsvurdering, Map<Periode, BigDecimal> perioderMedBeløp) {
        List<BeregningResultatPeriode> resulat = new ArrayList<>();
        resulat.addAll(beregnForForeldedePerioder(vurdertForeldelse, perioderMedBeløp));
        resulat.addAll(beregnForIkkeForeldedePerioder(kravgrunnlag, vilkårsvurdering, perioderMedBeløp));
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

    private Collection<BeregningResultatPeriode> beregnForIkkeForeldedePerioder(Kravgrunnlag431 kravgrunnlag, VilkårVurderingEntitet vilkårsvurdering, Map<Periode, BigDecimal> kravbeløpPrPeriode) {
        return vilkårsvurdering.getPerioder()
            .stream()
            .map(p -> beregnIkkeForeldetPeriode(kravgrunnlag, p, kravbeløpPrPeriode))
            .collect(Collectors.toList());
    }

    private Collection<BeregningResultatPeriode> beregnForForeldedePerioder(VurdertForeldelse vurdertForeldelse, Map<Periode, BigDecimal> kravbeløpPrPeriode) {
        return vurdertForeldelse.getVurdertForeldelsePerioder()
            .stream()
            .filter(p -> ForeldelseVurderingType.FORELDET.equals(p.getForeldelseVurderingType()))
            .map(p -> beregnForeldetPeriode(kravbeløpPrPeriode, p))
            .collect(Collectors.toList());
    }

    private BeregningResultatPeriode beregnForeldetPeriode(Map<Periode, BigDecimal> beløpPerPeriode, VurdertForeldelsePeriode foreldelsePeriode) {
        Periode periode = foreldelsePeriode.getPeriode();
        BeregningResultatPeriode resultat = new BeregningResultatPeriode();
        resultat.setPeriode(periode);
        resultat.setFeilutbetaltBeløp(beløpPerPeriode.get(periode));
        resultat.setTilbakekrevingBeløp(BigDecimal.ZERO);
        resultat.setTilbakekrevingBeløpUtenRenter(BigDecimal.ZERO);
        resultat.setRenteBeløp(BigDecimal.ZERO);
        resultat.setAndelAvBeløp(BigDecimal.ZERO);
        resultat.setVurdering(AnnenVurdering.FORELDET);
        resultat.setSkattBeløp(BigDecimal.ZERO);
        resultat.setTilbakekrevingBeløpEtterSkatt(BigDecimal.ZERO);
        return resultat;
    }

    private BeregningResultatPeriode beregnIkkeForeldetPeriode(Kravgrunnlag431 kravgrunnlag, VilkårVurderingPeriodeEntitet vurdering, Map<Periode, BigDecimal> kravbeløpPrPeriode) {
        Periode periode = vurdering.getPeriode();
        BigDecimal kravbeløp = kravbeløpPrPeriode.get(periode);
        List<GrunnlagPeriodeMedSkattProsent> perioderMedSkattProsent = lagGrunnlagPeriodeMedSkattProsent(periode, kravgrunnlag);
        return TilbakekrevingBeregnerVilkår.beregn(vurdering, kravbeløp, perioderMedSkattProsent);
    }

    private List<GrunnlagPeriodeMedSkattProsent> lagGrunnlagPeriodeMedSkattProsent(Periode periode, Kravgrunnlag431 kravgrunnlag) {
        List<KravgrunnlagPeriode432> kgPerioder = new ArrayList<>(kravgrunnlag.getPerioder());
        kgPerioder.sort(Comparator.comparing(p -> p.getPeriode().getFom()));

        List<GrunnlagPeriodeMedSkattProsent> perioderMedSkattProsent = new ArrayList<>();
        for (KravgrunnlagPeriode432 kgPeriode : kgPerioder) {
            List<KravgrunnlagBelop433> kgBeløper = kgPeriode.getKravgrunnlagBeloper433().stream()
                .filter(kgBeløp -> KlasseType.YTEL.equals(kgBeløp.getKlasseType()))
                .collect(Collectors.toList());
            List<GrunnlagPeriodeMedSkattProsent> periodeMedSkattProsent = kgBeløper.stream()
                .map(kgBeløp -> {
                    BigDecimal maksTilbakekrevesBeløp = BeregnBeløpUtil.beregnBeløpForPeriode(kgBeløp.getTilbakekrevesBelop(), periode, kgPeriode.getPeriode());
                    return new GrunnlagPeriodeMedSkattProsent(kgPeriode.getPeriode(), maksTilbakekrevesBeløp, kgBeløp.getSkattProsent());
                })
                .collect(Collectors.toList());
            perioderMedSkattProsent.addAll(periodeMedSkattProsent);
        }
        return perioderMedSkattProsent;
    }

    private VedtakResultatType bestemVedtakResultat(BigDecimal tilbakekrevingBeløp, BigDecimal feilutbetaltBeløp) {
        if (tilbakekrevingBeløp.compareTo(BigDecimal.ZERO) == 0) {
            return VedtakResultatType.INGEN_TILBAKEBETALING;
        } else if (tilbakekrevingBeløp.compareTo(feilutbetaltBeløp) < 0) {
            return VedtakResultatType.DELVIS_TILBAKEBETALING;
        }
        return VedtakResultatType.FULL_TILBAKEBETALING;
    }

    private static <T> BigDecimal sum(Collection<T> liste, Function<T, BigDecimal> konverter) {
        return liste.stream()
            .map(konverter::apply)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private static void sjekkOgJusterTotalSkattBeløp(List<KravgrunnlagPeriode432> kgPerioder, List<BeregningResultatPeriode> resultat) {
        for (KravgrunnlagPeriode432 periode432 : kgPerioder) {
            List<BeregningResultatPeriode> bgPerioder = resultat.stream().filter(periode -> periode.getPeriode().overlapper(periode432.getPeriode())).collect(Collectors.toList());
            BigDecimal totalBeregnetSkattBeløp = BigDecimal.ZERO;
            for (BeregningResultatPeriode beregningResultatPeriode : bgPerioder) {
                totalBeregnetSkattBeløp = totalBeregnetSkattBeløp.add(beregningResultatPeriode.getSkattBeløp());
                BigDecimal diff = totalBeregnetSkattBeløp.subtract(periode432.getBeløpSkattMnd());
                if (diff.signum() > 0) {
                    beregningResultatPeriode.setSkattBeløp(beregningResultatPeriode.getSkattBeløp().subtract(diff));
                }
            }

        }
    }

}
