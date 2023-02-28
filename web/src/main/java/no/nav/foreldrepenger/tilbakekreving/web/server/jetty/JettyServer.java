package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import static org.eclipse.jetty.webapp.MetaInfConfiguration.CONTAINER_JAR_PATTERN;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.naming.NamingException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.jaspi.DefaultAuthConfigFactory;
import org.eclipse.jetty.security.jaspi.JaspiAuthenticatorFactory;
import org.eclipse.jetty.security.jaspi.provider.JaspiAuthConfigProvider;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.sikkerhet.jaspic.OidcAuthModule;

public class JettyServer {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private static final String CONTEXT_PATH = getContextPath();
    private static final String JETTY_SCAN_LOCATIONS = "^.*jersey-.*\\.jar$|^.*felles-.*\\.jar$|^.*/app\\.jar$";
    private static final String JETTY_LOCAL_CLASSES = "^.*/target/classes/|";


    /**
     * Legges først slik at alltid resetter context før prosesserer nye requests.
     * Kjøres først så ikke risikerer andre har satt Request#setHandled(true).
     */
    static final class ResetLogContextHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
            MDC.clear();
        }
    }

    private final Integer serverPort;

    public static void main(String[] args) throws Exception {
        jettyServer(args).bootStrap();
    }

    private static JettyServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyServer(ENV.getProperty("server.port", Integer.class, 8080));
    }

    protected JettyServer(int serverPort) {
        this.serverPort = serverPort;
        setContextAndCookiePath();
    }

    protected void bootStrap() throws Exception {
        konfigurerLogging();
        konfigurerSikkerhet();
        var dataSource = DatasourceUtil.createDatasource(30);
        konfigurerDatasource(dataSource);
        migrerDatabaser(dataSource);
        start();
    }

    private void konfigurerLogging() {
        //openhtmltopdf bruker java.util.logging og konfigurerer egen handler, overstyrer vha system property
        System.setProperty("xr.util-logging.handlers", "org.slf4j.bridge.SLF4JBridgeHandler");
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private void konfigurerSikkerhet() {
        if (ENV.isLocal()) {
            initTrustStore();
        }

        var factory = new DefaultAuthConfigFactory();
        factory.registerConfigProvider(new JaspiAuthConfigProvider(new OidcAuthModule()),
                "HttpServlet",
                "server " + CONTEXT_PATH,
                "OIDC Authentication");

        AuthConfigFactory.setFactory(factory);
    }

    private static void initTrustStore() {
        final var trustStorePathProp = "javax.net.ssl.trustStore";
        final var trustStorePasswordProp = "javax.net.ssl.trustStorePassword";

        var defaultLocation = ENV.getProperty("user.home", ".") + "/.modig/truststore.jks";
        var storePath = ENV.getProperty(trustStorePathProp, defaultLocation);
        var storeFile = new File(storePath);
        if (!storeFile.exists()) {
            throw new IllegalStateException("Finner ikke truststore i " + storePath
                    + "\n\tKonfrigurer enten som System property '" + trustStorePathProp + "' eller environment variabel '"
                    + trustStorePathProp.toUpperCase().replace('.', '_') + "'");
        }
        var password = ENV.getProperty(trustStorePasswordProp, "changeit");
        System.setProperty(trustStorePathProp, storeFile.getAbsolutePath());
        System.setProperty(trustStorePasswordProp, password);
    }

    private void konfigurerDatasource(DataSource dataSource) throws NamingException {
        new EnvEntry("jdbc/defaultDS", dataSource);
    }

    private void migrerDatabaser(DataSource dataSource) {
        try {
            Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:/db/migration/defaultDS")
                    .table("schema_version")
                    .baselineOnMigrate(true)
                    .load()
                    .migrate();
        } catch (FlywayException e) {
            LOG.error("Feil under migrering av databasen.");
            throw e;
        }
    }

    private void start() throws Exception {
        var server = new Server(getServerPort());
        server.setConnectors(createConnectors(server).toArray(new Connector[]{}));
        var handlers = new HandlerList(new ResetLogContextHandler(), createContext());
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    private List<Connector> createConnectors(Server server) {
        List<Connector> connectors = new ArrayList<>();
        var httpConnector = new ServerConnector(server, new HttpConnectionFactory(createHttpConfiguration()));
        httpConnector.setPort(getServerPort());
        connectors.add(httpConnector);
        return connectors;
    }

    protected HttpConfiguration createHttpConfiguration() {
        var httpConfig = new HttpConfiguration();
        // Add support for X-Forwarded headers
        httpConfig.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());
        return httpConfig;
    }

    private static ContextHandler createContext() throws IOException {
        var ctx = new WebAppContext();
        ctx.setParentLoaderPriority(true);
        // må hoppe litt bukk for å hente web.xml fra classpath i stedet for fra filsystem.
        String descriptor;
        try (var resource = Resource.newClassPathResource("/WEB-INF/web.xml")) {
            descriptor = resource.getURI().toURL().toExternalForm();
        }
        ctx.setDescriptor(descriptor);

        ctx.setContextPath(CONTEXT_PATH);

        var appname = ApplicationName.hvilkenTilbake();
        if (Fagsystem.K9TILBAKE.equals(appname)) {
            ctx.setBaseResource(createResourceCollection());
        } else {
            ctx.setResourceBase(".");
        }

        ctx.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        ctx.setAttribute(CONTAINER_JAR_PATTERN, String.format("%s%s", ENV.isLocal() ? JETTY_LOCAL_CLASSES : "", JETTY_SCAN_LOCATIONS));

        // WELD init
        ctx.addEventListener(new org.jboss.weld.environment.servlet.BeanManagerResourceBindingListener());
        ctx.addEventListener(new org.jboss.weld.environment.servlet.Listener());

        ctx.setSecurityHandler(createSecurityHandler());
        ctx.setThrowUnavailableOnStartupException(true);

        return ctx;
    }

    private static ResourceCollection createResourceCollection() {
        return new ResourceCollection(
                Resource.newClassPathResource("/META-INF/resources/webjars/"),
                Resource.newClassPathResource("/web"));
    }

    private static SecurityHandler createSecurityHandler() {
        var securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticatorFactory(new JaspiAuthenticatorFactory());
        var loginService = new JAASLoginService();
        loginService.setName("jetty-login");
        loginService.setLoginModuleName("jetty-login");
        loginService.setIdentityService(new DefaultIdentityService());
        securityHandler.setLoginService(loginService);
        return securityHandler;
    }

    private Integer getServerPort() {
        return this.serverPort;
    }

    private void setContextAndCookiePath() {
        var appname = ApplicationName.hvilkenTilbake();
        if (Fagsystem.K9TILBAKE.equals(appname)) {
            ContextPathHolder.instance(CONTEXT_PATH, "/k9");
        }
    }

    public static String getContextPath() {
        return Optional.ofNullable(ENV.getProperty("context.path"))
            .orElseGet(() -> {
                var appname = ApplicationName.hvilkenTilbake();
                return switch (appname) {
                    case FPTILBAKE -> "/fptilbake";
                    case K9TILBAKE -> "/k9/tilbake";
                    default -> throw new IllegalArgumentException("Ikke-støttet applikasjonsnavn: " + appname);
                };
            });

    }
}
