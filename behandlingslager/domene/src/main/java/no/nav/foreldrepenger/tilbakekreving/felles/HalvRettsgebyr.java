package no.nav.foreldrepenger.tilbakekreving.felles;

import java.math.BigDecimal;

public class HalvRettsgebyr {

    private  static final BigDecimal GEBYR = new BigDecimal(586);

    private HalvRettsgebyr(){
        // sonar
    }

    public static BigDecimal getGebyr() {
        return GEBYR;
    }
}
