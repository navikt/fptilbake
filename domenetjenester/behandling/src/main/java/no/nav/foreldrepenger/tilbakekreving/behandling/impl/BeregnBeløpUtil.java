package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.felles.Virkedager;

public class BeregnBeløpUtil {

    private BeregnBeløpUtil() {
        // for å hindre instansiering slik at SonarQube blir glad
    }

    public static BigDecimal beregnBeløpPrVirkedag(BigDecimal beløp, Periode periode) {
        int antallVirkedager = Virkedager.beregnAntallVirkedager(periode);
        return beløp.divide(BigDecimal.valueOf(antallVirkedager), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal beregnBeløp(Periode foreldetPeriode, Periode grunnlagPeriode, BigDecimal beløpPrVirkedag) {
        Optional<Periode> overlap = grunnlagPeriode.overlap(foreldetPeriode);
        if (overlap.isPresent()) {
            Periode overlapInterval = overlap.get();
            int antallVirkedager = Virkedager.beregnAntallVirkedager(overlapInterval);
            return beløpPrVirkedag.multiply(BigDecimal.valueOf(antallVirkedager));
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal beregnYtelseBeløp(BigDecimal tilbakekrevesBeløp, Periode feilutbetalingPeriode, Periode periode) {
        BigDecimal grunnlagBelopPerUkeDager = BeregnBeløpUtil.beregnBeløpPrVirkedag(tilbakekrevesBeløp, periode);
        BigDecimal ytelseBeløp = BeregnBeløpUtil.beregnBeløp(feilutbetalingPeriode, periode, grunnlagBelopPerUkeDager);
        return ytelseBeløp.setScale(0, RoundingMode.HALF_UP);
    }
}
