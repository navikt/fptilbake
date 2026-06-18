package no.nav.foreldrepenger.tilbakekreving.dbstoette;

import javax.sql.DataSource;

import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.jpa.NamingStandard;
import no.nav.vedtak.felles.testutilities.db.MigrationUtil;

/**
 * Initielt skjemaoppsett + migrering av unittest-skjemaer
 */
public final class TestDatabaseInit {

    private static final String TEST_DB_CONTAINER = Environment.current()
        .getProperty("testcontainer.test.db", String.class, "gvenzl/oracle-free:23-slim-faststart");

    private static final Environment ENV = Environment.current();

    private static DataSource ds;

    public static synchronized DataSource getDataSource() {
        if (!ENV.isLocal()) {
            throw new IllegalStateException("Forventer at denne migreringen bare kjøres lokalt");
        }
        if (ds == null) {
            var testDatabase = new OracleContainer(DockerImageName.parse(TEST_DB_CONTAINER)).withReuse(true);
            testDatabase.start();
            ds = MigrationUtil.createLocalBuildTestDataSource(testDatabase.getJdbcUrl(), testDatabase.getUsername(), testDatabase.getUsername());
            MigrationUtil.migrateLocalBuildTest(ds, getScriptLocation());
        }
        return ds;
    }

    private static String getScriptLocation() {
        var relativePath = "migreringer/src/main/resources" + NamingStandard.DEFAULT_DS_MIGRATION_PATH;
        return MigrationUtil.getScriptLocation(relativePath);
    }

}
