package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagBelop433;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

@ApplicationScoped
public class KravgrunnlagBeregningTjeneste {

    private KravgrunnlagRepository grunnlagRepository;

    KravgrunnlagBeregningTjeneste() {
        //for CDI proxy
    }

    @Inject
    public KravgrunnlagBeregningTjeneste(KravgrunnlagRepository grunnlagRepository) {
        this.grunnlagRepository = grunnlagRepository;
    }

    public Map<Periode, FordeltKravgrunnlagBeløp> fordelKravgrunnlagBeløpPåPerioder(Long behandlingId, List<Periode> perioder) {
        Kravgrunnlag431 kravgrunnlag = grunnlagRepository.finnKravgrunnlag(behandlingId);
        return fordelKravgrunnlagBeløpPåPerioder(kravgrunnlag, perioder);
    }

    public Map<Periode, FordeltKravgrunnlagBeløp> fordelKravgrunnlagBeløpPåPerioder(Kravgrunnlag431 kravgrunnlag, List<Periode> perioder) {
        var map = new HashMap<Periode, FordeltKravgrunnlagBeløp>();
        for (Periode periode : perioder) {
            BigDecimal feilutbetaltBeløp = beregnFeilutbetaltBeløp(kravgrunnlag, periode);
            BigDecimal utbetaltYtelseBeløp = beregnUtbetaltYtelseBeløp(kravgrunnlag, periode);
            BigDecimal riktigYtelseBeløp = beregnRiktigYtelseBeløp(kravgrunnlag, periode);
            map.put(periode, new FordeltKravgrunnlagBeløp(feilutbetaltBeløp, utbetaltYtelseBeløp, riktigYtelseBeløp));
        }
        return map;
    }

    public Map<Periode, BigDecimal> beregnFeilutbetaltBeløp(Long behandlingId, List<Periode> perioder) {
        Kravgrunnlag431 kravgrunnlag = grunnlagRepository.finnKravgrunnlag(behandlingId);
        return beregnFeilutbetaltBeløp(kravgrunnlag, perioder);
    }

    public Map<Periode, BigDecimal> beregnFeilutbetaltBeløp(Kravgrunnlag431 kravgrunnlag, List<Periode> perioder) {
        var map = new HashMap<Periode, BigDecimal>();
        for (Periode periode : perioder) {
            BigDecimal feilutbetaltBeløp = beregnFeilutbetaltBeløp(kravgrunnlag, periode);
            map.put(periode, feilutbetaltBeløp);
        }
        return map;
    }

    private BigDecimal beregnFeilutbetaltBeløp(Kravgrunnlag431 kravgrunnlag, Periode periode) {
        Function<KravgrunnlagPeriode432, BigDecimal> feilutbetaltBeløpUtleder = kgPeriode -> kgPeriode.getKravgrunnlagBeloper433().stream()
                .filter(kgBeløp -> kgBeløp.getKlasseType().equals(KlasseType.FEIL))
                .map(KravgrunnlagBelop433::getNyBelop)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return beregnBeløp(kravgrunnlag, periode, feilutbetaltBeløpUtleder);
    }

    /**
     * Utbetalt beløp er ikke justert med trekk, det er OK for vår bruk
     */
    private BigDecimal beregnUtbetaltYtelseBeløp(Kravgrunnlag431 kravgrunnlag, Periode periode) {
        Function<KravgrunnlagPeriode432, BigDecimal> feilutbetaltBeløpUtleder = kgPeriode -> kgPeriode.getKravgrunnlagBeloper433().stream()
                .filter(kgBeløp -> kgBeløp.getKlasseType().equals(KlasseType.YTEL))
                .map(KravgrunnlagBelop433::getOpprUtbetBelop)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return beregnBeløp(kravgrunnlag, periode, feilutbetaltBeløpUtleder);
    }

    /**
     * Riktig beløp er ikke justert med trekk, det er OK for vår bruk
     */
    private BigDecimal beregnRiktigYtelseBeløp(Kravgrunnlag431 kravgrunnlag, Periode periode) {
        Function<KravgrunnlagPeriode432, BigDecimal> feilutbetaltBeløpUtleder = kgPeriode -> kgPeriode.getKravgrunnlagBeloper433().stream()
                .filter(kgBeløp -> kgBeløp.getKlasseType().equals(KlasseType.YTEL))
                .map(KravgrunnlagBelop433::getNyBelop)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return beregnBeløp(kravgrunnlag, periode, feilutbetaltBeløpUtleder);
    }

    private BigDecimal beregnBeløp(Kravgrunnlag431 kravgrunnlag, Periode periode, Function<KravgrunnlagPeriode432, BigDecimal> beløpUtleder) {
        BeregnBeløpUtil beregnBeløpUtil = BeregnBeløpUtil.forFagområde(kravgrunnlag.getFagOmrådeKode());
        List<KravgrunnlagPeriode432> kgPerioder = new ArrayList<>(kravgrunnlag.getPerioder());
        kgPerioder.sort(Comparator.comparing(p -> p.getPeriode().getFom()));
        BigDecimal sum = BigDecimal.ZERO;
        for (KravgrunnlagPeriode432 kgPeriode : kgPerioder) {
            BigDecimal beløp = beløpUtleder.apply(kgPeriode);
            if (isNotZero(beløp)) {
                BigDecimal feilutbetaltBeløpPrVirkedag = beregnBeløpUtil.beregnBeløpPrVirkedag(beløp, kgPeriode.getPeriode());
                sum = sum.add(beregnBeløpUtil.beregnBeløp(periode, kgPeriode.getPeriode(), feilutbetaltBeløpPrVirkedag));
            }
        }

        return sum.setScale(0, RoundingMode.HALF_UP);
    }

    private static boolean isNotZero(BigDecimal verdi) {
        return verdi.signum() != 0;
    }

}
