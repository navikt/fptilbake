package no.nav.foreldrepenger.tilbakekreving.dbstoette;

import no.nav.vedtak.felles.jpa.jdbc.DataSourceHolder;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;

public class JpaExtension extends EntityManagerAwareExtension {

    static {
        var ds = TestDatabaseInit.getDataSource();
        DataSourceHolder.initialize(ds);
    }

}
