package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.felles.Ukedager;

public class BeregnBeløpUtil {

    private boolean forEngangsstønad;

    public static BeregnBeløpUtil forFagområde(FagOmrådeKode fagOmrådeKode) {
        return new BeregnBeløpUtil(fagOmrådeKode == FagOmrådeKode.ENGANGSSTØNAD);
    }

    public BeregnBeløpUtil(boolean forEngangsstønad) {
        this.forEngangsstønad = forEngangsstønad;
    }

    public BigDecimal beregnBeløpPrVirkedag(BigDecimal beløp, Periode periode) {
        int antallVirkedager = Ukedager.beregnAntallVirkedager(periode);
        if (forEngangsstønad && antallVirkedager == 0) { //Gjelder kun ved Engangsstønad (REFUTG) som treffer en ikke vanlig virkedag.
            return beløp;
        }
        return beløp.divide(BigDecimal.valueOf(antallVirkedager), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal beregnBeløp(Periode foreldetPeriode, Periode grunnlagPeriode, BigDecimal beløpPrVirkedag) {
        Optional<Periode> overlap = grunnlagPeriode.overlap(foreldetPeriode);
        if (overlap.isPresent()) {
            Periode overlapInterval = overlap.get();
            int antallVirkedager = Ukedager.beregnAntallVirkedager(overlapInterval);
            if (forEngangsstønad && antallVirkedager == 0) { //Gjelder kun ved Engangsstønad (REFUTG) som treffer en ikke vanlig virkedag.
                return beløpPrVirkedag;
            }
            return beløpPrVirkedag.multiply(BigDecimal.valueOf(antallVirkedager));
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal beregnBeløpForPeriode(BigDecimal tilbakekrevesBeløp, Periode feilutbetalingPeriode, Periode periode) {
        BigDecimal grunnlagBelopPerUkeDager = beregnBeløpPrVirkedag(tilbakekrevesBeløp, periode);
        BigDecimal ytelseBeløp = beregnBeløp(feilutbetalingPeriode, periode, grunnlagBelopPerUkeDager);
        return ytelseBeløp.setScale(0, RoundingMode.HALF_UP);
    }

}
