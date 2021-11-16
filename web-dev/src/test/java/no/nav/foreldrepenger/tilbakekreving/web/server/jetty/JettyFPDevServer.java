package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import no.nav.foreldrepenger.konfig.Environment;

public class JettyFPDevServer extends JettyServer {

    private static final Environment ENV = Environment.current();

    private static final String TRUSTSTORE_PASSW_PROP = "javax.net.ssl.trustStorePassword";
    private static final String TRUSTSTORE_PATH_PROP = "javax.net.ssl.trustStore";

    private static final String VTP_ARGUMENT = "--vtp";
    private static boolean vtp;

    public static void main(String[] args) throws Exception {
        /* holder ikke å konfigurere disse i fptilbake.application.properties, da den ikke leses av Environment-klassen */
        System.setProperty("abac.attributt.applikasjon", "no.nav.abac.attributter.foreldrepenger");
        System.setProperty("abac.attributt.fagsak", "no.nav.abac.attributter.foreldrepenger.fagsak");
        System.setProperty("abac.attributt.ventefrist", "no.nav.abac.attributter.foreldrepenger.fagsak.ventefrist");
        System.setProperty("abac.attributt.drift", "no.nav.abac.attributter.foreldrepenger.drift");
        System.setProperty("abac.attributt.batch", "no.nav.abac.attributter.foreldrepenger.batch");

        for (String arg : args) {
            if (arg.equals(VTP_ARGUMENT)) {
                vtp = true;
            }
        }

        new JettyFPDevServer(new JettyWebKonfigurasjon(8030)).bootStrap();
    }

    public JettyFPDevServer(JettyWebKonfigurasjon webKonfigurasjon) {
        super(webKonfigurasjon);
    }

    @Override
    protected void konfigurer() throws Exception {
        System.setProperty("conf", "../web/src/main/resources/jetty/");
        konfigurerLogback();
        super.konfigurer();
    }

    protected void konfigurerLogback() throws IOException {
        new File("./logs").mkdirs();
        System.setProperty("APP_LOG_HOME", "./logs");
        File logbackConfig = PropertiesUtils.lagLogbackConfig();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            configurator.doConfigure(logbackConfig.getAbsolutePath());
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    @Override
    protected void konfigurerMiljø() throws Exception {
        System.setProperty("develop-local", "true");
        PropertiesUtils.lagPropertiesFilFraTemplate();
        PropertiesUtils.initProperties(JettyFPDevServer.vtp);
    }

    @Override
    protected void konfigurerSikkerhet() {
        initCryptoStoreConfig();
        super.konfigurerSikkerhet();
    }

    private static void initCryptoStoreConfig() {
        String defaultLocation = ENV.getProperty("user.home", ".") + "/.modig/truststore.jks";

        String storePath = ENV.getProperty(JettyFPDevServer.TRUSTSTORE_PATH_PROP, defaultLocation);
        File storeFile = new File(storePath);
        if (!storeFile.exists()) {
            throw new IllegalStateException("Finner ikke truststore i " + storePath
                + "\n\tKonfigurer enten som System property \'" + JettyFPDevServer.TRUSTSTORE_PATH_PROP + "\' eller environment variabel \'"
                + JettyFPDevServer.TRUSTSTORE_PATH_PROP.toUpperCase().replace('.', '_') + "\'");
        }
        String password = ENV.getProperty(JettyFPDevServer.TRUSTSTORE_PASSW_PROP, "changeit");
        if (password == null) {
            throw new IllegalStateException("Passord for å aksessere store truststore i " + storePath + " er null");
        }

        System.setProperty(JettyFPDevServer.TRUSTSTORE_PATH_PROP, storeFile.getAbsolutePath());
        System.setProperty(JettyFPDevServer.TRUSTSTORE_PASSW_PROP, password);
    }

    @Override
    protected void konfigurerJndi() throws Exception {
        JettyDevDbKonfigurasjon.ConnectionHandler.settOppJndiDataSource(PropertiesUtils.getDBConnectionProperties());
        konfigurerJms();
    }

    @Override
    protected void migrerDatabaser() throws IOException {
        JettyDevDbKonfigurasjon.kjørMigreringFor(PropertiesUtils.getDBConnectionProperties());
    }

    @SuppressWarnings("resource")
    @Override
    protected List<Connector> createConnectors(AppKonfigurasjon appKonfigurasjon, Server server) {
        List<Connector> connectors = super.createConnectors(appKonfigurasjon, server);

        HttpConfiguration https = createHttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(new SslContextFactory.Server(), "http/1.1"),
            new HttpConnectionFactory(https));
        sslConnector.setPort(appKonfigurasjon.getSslPort());
        connectors.add(sslConnector);

        return connectors;
    }

    @Override
    protected WebAppContext createContext(AppKonfigurasjon appKonfigurasjon) throws IOException {
        WebAppContext webAppContext = super.createContext(appKonfigurasjon);
        // https://www.eclipse.org/jetty/documentation/9.4.x/troubleshooting-locked-files-on-windows.html
        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        return webAppContext;
    }

    @Override
    protected ResourceCollection createResourceCollection() throws IOException {
        return new ResourceCollection(
            Resource.newClassPathResource("/META-INF/resources/webjars/"),
            Resource.newClassPathResource("/web")
        );
    }
}
