package no.nav.foreldrepenger.tilbakekreving.dbstoette;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.konfig.Environment;

public final class Databaseskjemainitialisering {

    private static final Logger LOG = LoggerFactory.getLogger(Databaseskjemainitialisering.class);
    private static final Environment ENV = Environment.current();
    private static final String FLYWAY_SCHEMA_TABLE = "schema_version";

    public static final List<DBProperties> UNIT_TEST = List.of(cfg("fptilbake.default"));

    public static final List<DBProperties> DBA = List.of(cfg("fptilbake.dba"));

    public static void main(String[] args) {
        migrer();
    }

    public static void migrer() {
            migrer(DBA);
            migrer(UNIT_TEST);
    }

    public static void settJdniOppslag() {
        try {
            var props = defaultProperties();
            new EnvEntry("jdbc/" + props.getDatasource(), ds(props));
        } catch (Exception e) {
            throw new RuntimeException("Feil under registrering av JDNI-entry for default datasource", e);
        }
    }

    public static DBProperties defaultProperties() {
        return UNIT_TEST.stream()
                .filter(DBProperties::isDefaultDataSource)
                .findFirst()
                .orElseThrow();
    }

    private static void migrer(List<DBProperties> props) {
        props.forEach(p -> migrer(ds(p), p));
    }

    private static void migrer(DataSource ds, DBProperties props) {
        LOG.info("Migrerer {}", props.getSchema());
        var flyway = new Flyway(Flyway.configure()
            .dataSource(ds)
            .locations(scriptLocation(props))
            .table(FLYWAY_SCHEMA_TABLE)
            .baselineOnMigrate(true)
            .cleanOnValidationError(true)
        );

        if (!ENV.isLocal()) {
            throw new IllegalStateException("Forventer at denne migreringen bare kjøres lokalt");
        }
        flyway.migrate();
    }

    private static DBProperties cfg(String prefix) {
        String schema = ENV.getRequiredProperty(prefix + ".schema");
        return new DBProperties.Builder()
                .user(schema)
                .versjonstabell(FLYWAY_SCHEMA_TABLE)
                .password(schema)
                .datasource(ENV.getRequiredProperty(prefix + ".datasource"))
                .schema(schema)
                .defaultSchema(ENV.getProperty(prefix + ".defaultschema", schema))
                .defaultDataSource(ENV.getProperty(prefix + ".default", boolean.class, false))
                .migrateClean(ENV.getProperty(prefix + ".migrateclean", boolean.class, true))
                .url(ENV.getRequiredProperty(prefix + ".url"))
                .migrationScriptsFilesystemRoot(ENV.getRequiredProperty(prefix + ".ms")).build();
    }

    private static String scriptLocation(DBProperties props) {
        return Optional.ofNullable(props.getMigrationScriptsClasspathRoot())
                .map(p -> "classpath:/" + p + "/" + props.getSchema())
                .orElse(fileScriptLocation(props));
    }

    private static String fileScriptLocation(DBProperties props) {
        String relativePath = props.getMigrationScriptsFilesystemRoot() + props.getDatasource();
        File baseDir = new File(".").getAbsoluteFile();
        File location = new File(baseDir, relativePath);
        while (!location.exists()) {
            baseDir = baseDir.getParentFile();
            if (baseDir == null || !baseDir.isDirectory()) {
                throw new IllegalArgumentException("Klarte ikke finne : " + baseDir);
            }
            location = new File(baseDir, relativePath);
        }

        return "filesystem:" + location.getPath();
    }

    public static DataSource ds(DBProperties props) {
        var ds = new HikariDataSource(hikariConfig(props));
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                ds.close();
            }
        }));
        return ds;
    }

    private static HikariConfig hikariConfig(DBProperties props) {
        var cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getUrl());
        cfg.setUsername(props.getUser());
        cfg.setPassword(props.getPassword());
        cfg.setConnectionTimeout(1000);
        cfg.setMinimumIdle(0);
        cfg.setMaximumPoolSize(4);
        cfg.setAutoCommit(false);
        return cfg;
    }
}
