package no.nav.foreldrepenger.tilbakekreving.dbstoette;

import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;

public class JpaExtension extends EntityManagerAwareExtension {

    private static final OracleContainer TEST_DATABASE;

    static {
        TEST_DATABASE = new OracleContainer(DockerImageName.parse(TestDatabaseInit.TEST_DB_CONTAINER)).withReuse(true);
        TEST_DATABASE.start();
        TestDatabaseInit.settOppDatasourceOgMigrer(TEST_DATABASE.getJdbcUrl(), TEST_DATABASE.getUsername(), TEST_DATABASE.getPassword());
    }

}
