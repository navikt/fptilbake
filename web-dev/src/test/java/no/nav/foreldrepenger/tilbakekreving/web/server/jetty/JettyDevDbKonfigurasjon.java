package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class JettyDevDbKonfigurasjon {
    private static final ObjectMapper OM;
    private static final String FLYWAY_SCHEMA_TABLE = "schema_version";

    static {
        OM = new ObjectMapper();

        OM.registerModule(new JavaTimeModule());
        OM.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
        OM.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
        OM.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    }

    private String datasource;
    private String schema;
    private String url;
    private String migrationScriptsFilesystemRoot;
    private boolean defaultDataSource;

    JettyDevDbKonfigurasjon() {
    }

    public String getDatasource() {
        return datasource;
    }

    public String getSchema() {
        return schema;
    }

    public String getUrl() {
        return url;
    }

    public String getMigrationScriptsFilesystemRoot() {
        return migrationScriptsFilesystemRoot;
    }

    public boolean getDefaultDataSource() {
        return defaultDataSource;
    }

    public static List<JettyDevDbKonfigurasjon> fraFil(File file) throws IOException {
        ObjectReader reader = JettyDevDbKonfigurasjon.OM.readerFor(JettyDevDbKonfigurasjon.class);
        try (MappingIterator<JettyDevDbKonfigurasjon> iterator = reader.readValues(file)) {
            return iterator.readAll();
        }
    }

    public String getUser() {
        return getSchema();
    }

    public String getPassword() {
        return getSchema();
    }

    /**
     * Migrering kjøres i vilkårlig rekkefølge. Hvis bruker/skjema angitt i {@link DBConnectionProperties}
     * ikke finnes, opprettes den
     */
    static void kjørMigreringFor(List<JettyDevDbKonfigurasjon> connectionProperties) {
        connectionProperties.forEach(JettyDevDbKonfigurasjon::kjørMigrering);
    }

    public static void kjørMigrering(JettyDevDbKonfigurasjon connectionProps) {
        DataSource dataSource = ConnectionHandler.opprettFra(connectionProps);
        migrer(dataSource, connectionProps);
    }

    private static void migrer(DataSource dataSource,
                               JettyDevDbKonfigurasjon connectionProperties) {

        class FlywayKonfig {

            String getMigrationScriptLocation(JettyDevDbKonfigurasjon connectionProperties) {
                String relativePath = connectionProperties.getMigrationScriptsFilesystemRoot() + connectionProperties.getDatasource();
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
        }
        var flywayConfiguration = Flyway.configure()
            .dataSource(dataSource)
            .table(FLYWAY_SCHEMA_TABLE)
            .baselineOnMigrate(true);

        FlywayKonfig flywayKonfig = new FlywayKonfig();
        final String scriptLocation = flywayKonfig.getMigrationScriptLocation(connectionProperties);
        if (scriptLocation != null) {
            flywayConfiguration.locations(scriptLocation);
        } else {
            /**
             * Default leter flyway etter classpath:db/migration.
             * Her vet vi at vi ikke skal lete i classpath
             */
            flywayConfiguration.locations("denne/stien/finnes/ikke");
        }

        var flyway = new Flyway(flywayConfiguration);
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            e.printStackTrace();
            flyway.clean();
            flyway.migrate();
        }
    }

    /** Håndter oppsett av datasource. */
    static class ConnectionHandler {

        private static Map<String, DataSource> cache = new ConcurrentHashMap<>();

        private ConnectionHandler() {
        }

        static void settOppJndiDataSource(Collection<JettyDevDbKonfigurasjon> connProps) {
            connProps.forEach(c -> settOppJndiDataSource(c));
        }

        static void settOppJndiDataSource(JettyDevDbKonfigurasjon defaultConnectionProperties) {
            DataSource dataSource = opprettFra(defaultConnectionProperties);
            try {
                new EnvEntry("jdbc/" + defaultConnectionProperties.getDatasource(), dataSource); // NOSONAR
            } catch (NamingException e) {
                throw new RuntimeException("Feil under registrering av JDNI-entry for default datasource", e); // NOSONAR
            }
        }

        static synchronized DataSource opprettFra(JettyDevDbKonfigurasjon dbProperties) {

            if (cache.containsKey(dbProperties.getDatasource())) {
                return cache.get(dbProperties.getDatasource());
            }

            DataSource ds = opprettDatasource(dbProperties);
            cache.put(dbProperties.getDatasource(), ds);

            return ds;
        }

        private static DataSource opprettDatasource(JettyDevDbKonfigurasjon dbProperties) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbProperties.getUrl());
            config.setUsername(dbProperties.getUser());
            config.setPassword(dbProperties.getPassword());

            config.setConnectionTimeout(1000);
            config.setMinimumIdle(2);
            config.setMaximumPoolSize(4);
            config.setConnectionTestQuery("select 1 from dual");
            config.setDriverClassName("oracle.jdbc.OracleDriver");

            Properties dsProperties = new Properties();
            config.setDataSourceProperties(dsProperties);

            HikariDataSource hikariDataSource = new HikariDataSource(config);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> hikariDataSource.close()));

            return hikariDataSource;
        }
    }

}
