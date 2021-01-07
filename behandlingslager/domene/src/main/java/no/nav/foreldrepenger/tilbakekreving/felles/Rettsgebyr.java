package no.nav.foreldrepenger.tilbakekreving.felles;

import java.math.BigDecimal;

public class Rettsgebyr {

    public static final int GEBYR = 1199;

    private Rettsgebyr(){
        // sonar
    }
    public static BigDecimal getGebyr() {
        return BigDecimal.valueOf(GEBYR);
    }
}
