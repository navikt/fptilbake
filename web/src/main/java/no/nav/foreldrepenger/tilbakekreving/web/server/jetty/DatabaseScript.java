package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseScript {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseScript.class);
    private static final String FLYWAY_SCHEMA_TABLE = "schema_version";

    private final DataSource dataSource;
    private final String locations;

    public DatabaseScript(DataSource dataSource, String locations) {
        this.dataSource = dataSource;
        this.locations = locations;
    }

    public void migrate() {
        Flyway flyway = new Flyway(Flyway.configure()
            .dataSource(dataSource)
            .locations(locations)
            .table(FLYWAY_SCHEMA_TABLE)
            .baselineOnMigrate(true));

        try {
            flyway.migrate();
        } catch (FlywayException e) {  // NOSONAR
            LOG.error("Feil under migrering av databasen.");
            throw e;
        }

    }
}
