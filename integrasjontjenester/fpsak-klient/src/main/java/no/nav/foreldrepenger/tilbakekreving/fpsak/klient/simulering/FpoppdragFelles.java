package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.simulering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;

class FpoppdragFelles {

    private static final Logger logger = LoggerFactory.getLogger(FpoppdragFelles.class);

    static final String FPOPPDRAG_BASE_URL = "http://fpoppdrag/fpoppdrag/api";
    static final String FPOPPDRAG_OVERRIDE_URL = "fpoppdrag.override.url";

    private FpoppdragFelles(){

    }

    static String getFpoppdragBaseUrl() {
        String overrideUrl = Environment.current().getProperty(FpoppdragFelles.FPOPPDRAG_OVERRIDE_URL);
        if (overrideUrl != null && !overrideUrl.isEmpty()) {
            logger.info("Overstyrte URL til fpoppdrag til {}", overrideUrl);
            return overrideUrl;
        } else {
            return FpoppdragFelles.FPOPPDRAG_BASE_URL;
        }

    }
}
