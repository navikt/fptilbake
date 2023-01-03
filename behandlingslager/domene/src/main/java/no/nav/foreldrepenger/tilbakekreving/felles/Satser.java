package no.nav.foreldrepenger.tilbakekreving.felles;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Satser {

    //se https://www.skatteetaten.no/satser/rettsgebyr/
    private static final BigDecimal RETTSGEBYR = BigDecimal.valueOf(1243);
    private static final BigDecimal HALVT_RETTSGEBYR = BigDecimal.valueOf(622);

    //se https://www.skatteetaten.no/satser/grunnbelopet-i-folketrygden/
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(106399);
    private static final BigDecimal HALVT_GRUNNBELØP = GRUNNBELØP.divide(BigDecimal.valueOf(2), 0, RoundingMode.UP);

    private Satser() {
        // sonar
    }

    public static BigDecimal rettsgebyr() {
        return RETTSGEBYR;
    }

    public static BigDecimal rettsgebyr(int antall) {
        return RETTSGEBYR.multiply(BigDecimal.valueOf(antall));
    }

    public static BigDecimal halvtRettsgebyr() {
        return HALVT_RETTSGEBYR;
    }

    public static BigDecimal grunnbeløp() {
        return GRUNNBELØP;
    }

    public static BigDecimal halvtGrunnbeløp() {
        return HALVT_GRUNNBELØP;
    }
}
