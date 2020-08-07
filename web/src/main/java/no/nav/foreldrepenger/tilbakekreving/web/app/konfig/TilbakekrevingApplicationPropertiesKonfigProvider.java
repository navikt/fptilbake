package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import static java.lang.System.getenv;
import static no.nav.vedtak.konfig.StandardPropertySource.APP_PROPERTIES;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.Dependent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.EnvPropertiesKonfigVerdiProvider;
import no.nav.vedtak.konfig.PropertiesKonfigVerdiProvider;
import no.nav.vedtak.util.env.Environment;

@Dependent
/**
 * egen ApplicationPropertiesKonfigProvider for tilbakekreving, for å understøtte ulik konfig for k9 og fp
 */
public class TilbakekrevingApplicationPropertiesKonfigProvider extends PropertiesKonfigVerdiProvider {

    static class Init {
        // lazy init singleton
        static final Properties PROPS = lesFra();
        private static final String SUFFIX = ".properties";
        private static final String PREFIX = "application";

        private static Properties lesFra() {
            return lesFra(namespaceKonfig(), lesFra(clusterKonfig(), lesFra("", new Properties())));
        }

        private static Properties lesFra(String infix, Properties p) {
            if (infix == null) {
                return p;
            }
            String navn = finnApplikasjonsnavn() + "." + PREFIX + infix + SUFFIX;
            try (var is = TilbakekrevingApplicationPropertiesKonfigProvider.class.getClassLoader().getResourceAsStream(navn)) {
                if (is != null) {
                    LOG.info("Laster properties fra {}", navn);
                    p.load(is);
                    return p;
                }
            } catch (IOException e) {
                LOG.info("Propertyfil {} ikke lesbar", navn);
            }
            LOG.info("Propertyfil {} ikke funnet", navn);
            return p;
        }

        private static String finnApplikasjonsnavn() {
            String envAppName = Environment.current().getProperty("APP_NAME");
            if (envAppName != null) {
                return envAppName;
            }
            String systemPropertiesAppName = System.getProperty("app.name");
            if (systemPropertiesAppName != null) {
                return systemPropertiesAppName;
            }
            throw new IllegalArgumentException("Fant ikke applikasjonsnavnet.");
        }

    }

    private static final int PRIORITET = EnvPropertiesKonfigVerdiProvider.PRIORITET + 10;
    private static final String LOCAL = "local";
    private static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";
    private static final String NAIS_NAMESPACE_NAME = "NAIS_NAMESPACE";

    private static final Logger LOG = LoggerFactory.getLogger(TilbakekrevingApplicationPropertiesKonfigProvider.class);

    public TilbakekrevingApplicationPropertiesKonfigProvider() {
        this(Init.PROPS);
    }

    protected TilbakekrevingApplicationPropertiesKonfigProvider(Properties props) {
        super(props, APP_PROPERTIES);
    }

    @Override
    public int getPrioritet() {
        return PRIORITET;
    }

    private static String clusterKonfig() {
        return "-" + clusterName();
    }

    private static String namespaceKonfig() {
        var namespaceName = namespaceName();
        if (namespaceName != null) {
            return clusterKonfig() + "-" + namespaceName;
        }
        return null;
    }

    private static String clusterName() {
        return Optional.ofNullable(getenv(NAIS_CLUSTER_NAME))
            .orElse(LOCAL);
    }

    private static String namespaceName() {
        return Optional.ofNullable(getenv(NAIS_NAMESPACE_NAME))
            .orElse(null);
    }

}
