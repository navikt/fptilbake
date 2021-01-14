package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class BeregnBeløpUtil {

    private boolean helgHarYtelsedager;

    public static BeregnBeløpUtil forFagområde(FagOmrådeKode fagOmrådeKode) {
        return new BeregnBeløpUtil(fagOmrådeKode == FagOmrådeKode.OMSORGSPENGER || fagOmrådeKode == FagOmrådeKode.ENGANGSSTØNAD);
    }

    public BeregnBeløpUtil(boolean helgHarYtelsedager) {
        this.helgHarYtelsedager = helgHarYtelsedager;
    }

    public BigDecimal beregnBeløpPrYtelsedag(BigDecimal beløp, Periode periode) {
        int antallYtelsedager = antallYtelsedager(periode);
        return beløp.divide(BigDecimal.valueOf(antallYtelsedager), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal beregnBeløp(Periode foreldetPeriode, Periode grunnlagPeriode, BigDecimal beløpPrYtelsedag) {
        Optional<Periode> muligOverlapp = grunnlagPeriode.overlap(foreldetPeriode);
        if (muligOverlapp.isPresent()) {
            Periode overlapp = muligOverlapp.get();
            int antallYtelsedagerOverlapp = antallYtelsedager(overlapp);
            return beløpPrYtelsedag.multiply(BigDecimal.valueOf(antallYtelsedagerOverlapp));
        }
        return BigDecimal.ZERO;
    }

    public int antallYtelsedager(Periode periode) {
        return helgHarYtelsedager ? periode.antallKalenderdager() : periode.antallUkedager();
    }

    public BigDecimal beregnBeløpForPeriode(BigDecimal tilbakekrevesBeløp, Periode feilutbetalingPeriode, Periode periode) {
        BigDecimal grunnlagBelopPerUkeDager = beregnBeløpPrYtelsedag(tilbakekrevesBeløp, periode);
        BigDecimal ytelseBeløp = beregnBeløp(feilutbetalingPeriode, periode, grunnlagBelopPerUkeDager);
        return ytelseBeløp.setScale(0, RoundingMode.HALF_UP);
    }
}
