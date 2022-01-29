package no.nav.journalpostapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;

class JournalpostApiPlassering {

    private static final Logger logger = LoggerFactory.getLogger(JournalpostApiPlassering.class);

    private JournalpostApiPlassering() {

    }

    static String getBaseUrl() {
        String overrideUrl = Environment.current().getProperty("journalpostapi.override.url");
        if (overrideUrl != null && !overrideUrl.isEmpty()) {
            logger.info("Overstyrte URL til dokarkiv til {}", overrideUrl);
            return overrideUrl;
        } else {
            return "http://dokarkiv";
        }
    }
}
