package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import no.nav.foreldrepenger.felles.jms.precond.PreconditionChecker;
import no.nav.foreldrepenger.felles.jms.precond.PreconditionCheckerResult;

import java.sql.SQLException;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;


@ApplicationScoped
class DatabasePreconditionChecker implements PreconditionChecker {

    @Resource(mappedName = "jdbc/defaultDS")
    private DataSource dataSource;

    DatabasePreconditionChecker() {
        // for CDI proxy
    }

    @Override
    public PreconditionCheckerResult check() {
        try (var ignored = dataSource.getConnection()) {
            // Connection pool validerer connections for oss, så trenger ikke gjøre noen spørring her (ønsker
            // bare å se om db er tilgjengelig)
            return PreconditionCheckerResult.fullfilled();
        } catch (SQLException e) { // NOSONAR
            return PreconditionCheckerResult.notFullfilled(e.getMessage());
        }
    }
}
