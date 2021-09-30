package no.nav.foreldrepenger.tilbakekreving.dbstoette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;

public class FptilbakeEntityManagerAwareExtension extends EntityManagerAwareExtension {

    private static final Logger LOG = LoggerFactory.getLogger(FptilbakeEntityManagerAwareExtension.class);
    private static final boolean isNotRunningUnderMaven = Environment.current().getProperty("maven.cmd.line.args") == null;

    static {
        if (isNotRunningUnderMaven) {
            LOG.info("Kjører IKKE under maven");
            // prøver alltid migrering hvis endring, ellers funker det dårlig i IDE.
            Databaseskjemainitialisering.migrer();
        }
        Databaseskjemainitialisering.settJdniOppslag();
    }

}
