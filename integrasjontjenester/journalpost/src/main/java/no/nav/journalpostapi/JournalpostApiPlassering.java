package no.nav.journalpostapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.PropertyUtil;

class JournalpostApiPlassering {

    private static final Logger logger = LoggerFactory.getLogger(JournalpostApiPlassering.class);

    static final String BASE_URL = "http://dokarkiv";
    static final String OVERRIDE_URL = "journalpostapi.override.url";

    private JournalpostApiPlassering(){

    }

    static String getBaseUrl() {
        String overrideUrl = PropertyUtil.getProperty(JournalpostApiPlassering.OVERRIDE_URL);
        if (overrideUrl != null && !overrideUrl.isEmpty()) {
            logger.info("Overstyrte URL til dokarkiv til {}", overrideUrl);
            return overrideUrl;
        } else {
            return JournalpostApiPlassering.BASE_URL;
        }
    }
}
