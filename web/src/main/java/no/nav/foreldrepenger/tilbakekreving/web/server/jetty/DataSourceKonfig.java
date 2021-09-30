package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.konfig.Environment;

class DataSourceKonfig {

    private static final String location = "classpath:/db/migration/";
    private DBConnProp defaultDatasource;
    private List<DBConnProp> dataSources;

    DataSourceKonfig() {
        defaultDatasource = new DBConnProp(createDatasource("defaultDS"), location + "defaultDS");
        dataSources = Arrays.asList(defaultDatasource);
    }

    private DataSource createDatasource(String dataSourceName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Environment.current().getProperty(dataSourceName + ".url"));
        config.setUsername(Environment.current().getProperty(dataSourceName + ".username"));
        config.setPassword(Environment.current().getProperty(dataSourceName + ".password")); // NOSONAR false positive

        config.setConnectionTimeout(1000);
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(30);
        config.setConnectionTestQuery("select 1 from dual");
        config.setDriverClassName("oracle.jdbc.OracleDriver");

        Properties dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        return new HikariDataSource(config);
    }

    DBConnProp getDefaultDatasource() {
        return defaultDatasource;
    }

    List<DBConnProp> getDataSources() {
        return dataSources;
    }

    static final class DBConnProp {
        private DataSource datasource;
        private String migrationScripts;

        public DBConnProp(DataSource datasource, String migrationScripts) {
            this.datasource = datasource;
            this.migrationScripts = migrationScripts;
        }

        public DataSource getDatasource() {
            return datasource;
        }

        public String getMigrationScripts() {
            return migrationScripts;
        }
    }

}
