package no.nav.foreldrepenger.tilbakekreving.dbstoette;

import static java.lang.Runtime.getRuntime;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.konfig.Environment;

public final class Databaseskjemainitialisering {

    private static final Logger LOG = LoggerFactory.getLogger(Databaseskjemainitialisering.class);
    private static final AtomicBoolean GUARD_UNIT_TEST_SKJEMAER = new AtomicBoolean();

    private static final Environment ENV = Environment.current();
    public static final String USER = "fptilbake_unit";
    private static final String DB_SCRIPT_LOCATION = "/db/migration/";

    private static final DataSource DS = settJdniOppslag(USER);
    private static final String SCHEMA = "defaultDS";

    public static void main(String[] args) {
        //brukes i mvn clean install
        migrerUnittestSkjemaer();
    }

    public static DataSource initUnitTestDataSource() {
        if (DS != null) {
            return DS;
        }
        settJdniOppslag(USER);
        return DS;
    }

    public static void migrerUnittestSkjemaer() {
        if (GUARD_UNIT_TEST_SKJEMAER.compareAndSet(false, true)) {
            var flyway = Flyway.configure()
                    .dataSource(createDs(USER))
                    .locations(DB_SCRIPT_LOCATION + SCHEMA)
                    .table("schema_version")
                    .baselineOnMigrate(true)
                    .load();
            try {
                if (!ENV.isLocal()) {
                    throw new IllegalStateException("Forventer at denne migreringen bare kjøres lokalt");
                }
                flyway.migrate();
            } catch (FlywayException fwe) {
                try {
                    // prøver igjen
                    flyway.clean();
                    flyway.migrate();
                } catch (FlywayException fwe2) {
                    throw new IllegalStateException("Migrering feiler", fwe2);
                }
            }
        }
    }

    private static synchronized DataSource settJdniOppslag(String user) {
        var ds = createDs(user);
        try {
            new EnvEntry("jdbc/defaultDS", ds); // NOSONAR
            return ds;
        } catch (NamingException e) {
            throw new IllegalStateException("Feil under registrering av JDNI-entry for defaultDS", e); // NOSONAR
        }
    }

    private static DataSource createDs(String user) {
        Objects.requireNonNull(user, "user");
        var cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:XE");
        cfg.setUsername(user);
        cfg.setPassword(user);
        cfg.setConnectionTimeout(1500);
        cfg.setValidationTimeout(120L * 1000L);
        cfg.setMaximumPoolSize(4);
        cfg.setAutoCommit(false);
        var ds = new HikariDataSource(cfg);
        getRuntime().addShutdownHook(new Thread(ds::close));
        return ds;
    }
}
