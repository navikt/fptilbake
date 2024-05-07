package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Function;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.felles.Ukedager;

public class BeregnBeløpUtil {

    private Function<Periode, Integer> periodeTilDager;

    public static BeregnBeløpUtil forFagområde(FagOmrådeKode fagOmrådeKode) {
        return new BeregnBeløpUtil(fagOmrådeKode);
    }

    private BeregnBeløpUtil(FagOmrådeKode fagOmrådeKode) {
        this(fagOmrådeKode == FagOmrådeKode.ENGANGSSTØNAD || fagOmrådeKode == FagOmrådeKode.OMSORGSPENGER);
    }

    private BeregnBeløpUtil(boolean brukAlleDager) {
        periodeTilDager = brukAlleDager
            ? (Periode p) -> (int) ChronoUnit.DAYS.between(p.getFom(), p.getTom()) + 1
            : Ukedager::beregnAntallVirkedager;
    }

    public BigDecimal beregnBeløpPrDag(BigDecimal beløp, Periode periode) {
        int antallDager = periodeTilDager.apply(periode);
        return beløp.divide(BigDecimal.valueOf(antallDager), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal beregnBeløp(Periode foreldetPeriode, Periode grunnlagPeriode, BigDecimal beløpPrVirkedag) {
        Optional<Periode> overlap = grunnlagPeriode.overlap(foreldetPeriode);
        if (overlap.isPresent()) {
            Periode overlapInterval = overlap.get();
            int antallDager = periodeTilDager.apply(overlapInterval);
            return beløpPrVirkedag.multiply(BigDecimal.valueOf(antallDager));
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal beregnBeløpForPeriode(BigDecimal tilbakekrevesBeløp, Periode feilutbetalingPeriode, Periode periode) {
        BigDecimal grunnlagBelopPerUkeDager = beregnBeløpPrDag(tilbakekrevesBeløp, periode);
        BigDecimal ytelseBeløp = beregnBeløp(feilutbetalingPeriode, periode, grunnlagBelopPerUkeDager);
        return ytelseBeløp.setScale(0, RoundingMode.HALF_UP);
    }

}
