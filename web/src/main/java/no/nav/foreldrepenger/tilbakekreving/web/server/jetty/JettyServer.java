package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import static org.eclipse.jetty.ee11.webapp.MetaInfConfiguration.CONTAINER_JAR_PATTERN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.security.auth.message.config.AuthConfigFactory;

import org.eclipse.jetty.ee11.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee11.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee11.security.jaspi.DefaultAuthConfigFactory;
import org.eclipse.jetty.ee11.security.jaspi.JaspiAuthenticatorFactory;
import org.eclipse.jetty.ee11.security.jaspi.provider.JaspiAuthConfigProvider;
import org.eclipse.jetty.ee11.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.ee11.webapp.WebAppContext;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.jaas.JAASLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.ApiConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.ForvaltningApiConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.InternalApiConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.FpServiceStarterListener;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.ContextPathHolder;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.jaspic.OidcAuthModule;
import no.nav.vedtak.felles.jpa.NamingStandard;
import no.nav.vedtak.felles.jpa.flyway.FlywayUtil;
import no.nav.vedtak.felles.jpa.jdbc.DataSourceHolder;
import no.nav.vedtak.felles.jpa.jdbc.DatasourceUtil;
import no.nav.vedtak.log.metrics.MetricsUtil;
import no.nav.vedtak.server.jetty.DataSourceShutdownListener;
import no.nav.vedtak.server.jetty.JettyServerBuilder;

public class JettyServer {

    private static final Environment ENV = Environment.current();
    private static final String CONTEXT_PATH = getContextPath();
    private static final String JETTY_SCAN_LOCATIONS = "^.*jersey-.*\\.jar$|^.*felles-.*\\.jar$|^.*/app\\.jar$";
    private static final String JETTY_LOCAL_CLASSES = "^.*/target/classes/|";


    private final Integer serverPort;

    static void main() throws Exception {
        jettyServer().bootStrap();
    }

    private static JettyServer jettyServer() {
        return new JettyServer(ENV.getProperty("server.port", Integer.class, 8080));
    }

    protected JettyServer(int serverPort) {
        this.serverPort = serverPort;
        ApplicationName.hvilkenTilbake(); // Sørger for at den initialiseres fra environment
        if (Fagsystem.K9TILBAKE.equals(ApplicationName.hvilkenTilbake())) {
            setContextAndCookiePath();
        }
    }

    protected void bootStrap() throws Exception {
        MetricsUtil.init();
        konfigurerLogging();
        konfigurerSystembruker();
        konfigurerSikkerhet();
        createDatasourceMigrer();

        if (Fagsystem.K9TILBAKE.equals(ApplicationName.hvilkenTilbake())) {
            startK9tilbake();
        } else {
            startFptilbake();
        }
    }

    private void konfigurerLogging() {
        //openhtmltopdf bruker java.util.logging og konfigurerer egen handler, overstyrer vha system property
        System.setProperty("xr.util-logging.handlers", "org.slf4j.bridge.SLF4JBridgeHandler");
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    /* Brukes kun for å kunne samhandle med Økonomi via JMS */
    private static void konfigurerSystembruker() {
        settSystembrukerVerdiHvisMangler("systembruker.username", "username");
        settSystembrukerVerdiHvisMangler("systembruker.password", "password");
    }

    private static void settSystembrukerVerdiHvisMangler(String key, String filNavn) {
        if (ENV.getProperty(key) == null) {
            System.getProperties().computeIfAbsent(key, _ -> VaultUtil.lesFilVerdi("serviceuser", filNavn));
        }
    }

    private static void createDatasourceMigrer() {
        var jdbc = hentEllerBeregnVerdiHvisMangler("defaultDS.url", "defaultDSconfig", "jdbc_url");
        var username = hentEllerBeregnVerdiHvisMangler("defaultDS.username", "defaultDS", "username");
        var password = hentEllerBeregnVerdiHvisMangler("defaultDS.password", "defaultDS", "password");
        var dataSource = DatasourceUtil.oracleDataSource(jdbc, username, password, 30);
        DataSourceHolder.initialize(dataSource);
        FlywayUtil.migrateLegacyOracle(dataSource, NamingStandard.DEFAULT_DS_MIGRATION_CLASSPATH);
    }

    /* Denne gir lazy loading og feiler ikke ved lokalt kjøring uten vault mount */
    private static String hentEllerBeregnVerdiHvisMangler(String key, String mappeNavn, String filNavn) {
        if (ENV.getProperty(key) == null) {
            System.getProperties().computeIfAbsent(key, _ -> VaultUtil.lesFilVerdi(mappeNavn, filNavn));
        }
        return ENV.getRequiredProperty(key);
    }

    private void startFptilbake() throws Exception {
        var server = JettyServerBuilder.builder()
            .port(getServerPort())
            .contextPath(CONTEXT_PATH)
            .withForwardedRequestCustomizer()
            .addEventListener(new FpServiceStarterListener())
            .addEventListener(new DataSourceShutdownListener(DataSourceHolder::close))
            .registerRestApp(InternalApiConfig.API_URI, InternalApiConfig.class)
            .registerRestApp(ApiConfig.API_URI, ApiConfig.class)
            .registerRestApp(ForvaltningApiConfig.API_URI, ForvaltningApiConfig.class)
            .build();
        server.start();
        server.join();
    }

    private void konfigurerSikkerhet() {
        if (Fagsystem.K9TILBAKE.equals(ApplicationName.hvilkenTilbake())) {
            var factory = new DefaultAuthConfigFactory();
            factory.registerConfigProvider(new JaspiAuthConfigProvider(new OidcAuthModule()),
                "HttpServlet",
                "server " + CONTEXT_PATH,
                "OIDC Authentication");

            AuthConfigFactory.setFactory(factory);
        }
    }

    private void startK9tilbake() throws Exception {
        var server = new Server(getServerPort());
        server.setConnectors(createConnectors(server).toArray(new Connector[]{}));
        if (Fagsystem.K9TILBAKE.equals(ApplicationName.hvilkenTilbake())) {
            var handlers = new Handler.Sequence(new ResetLogContextHandler(), createContext());
            server.setHandler(handlers);
        } else {
            server.setHandler(createContext());
        }
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
        String baseResource;
        try (var factory = ResourceFactory.closeable()) {
            var resource = factory.newClassLoaderResource("/WEB-INF/web.xml", false);
            descriptor = resource.getURI().toURL().toExternalForm();
            baseResource = factory.newResource(".").getRealURI().toURL().toExternalForm();
        }
        ctx.setDescriptor(descriptor);

        ctx.setContextPath(CONTEXT_PATH);

        var appname = ApplicationName.hvilkenTilbake();
        if (Fagsystem.K9TILBAKE.equals(appname)) {
            ctx.setBaseResource(createResourceCollection(ctx));
        } else {
            ctx.setBaseResourceAsString(baseResource);
        }
        ctx.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        ctx.setAttribute(CONTAINER_JAR_PATTERN, String.format("%s%s", ENV.isLocal() ? JETTY_LOCAL_CLASSES : "", JETTY_SCAN_LOCATIONS));
        // Enable Weld + CDI
        ctx.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
        ctx.addServletContainerInitializer(new CdiServletContainerInitializer());
        ctx.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());

        ctx.setSecurityHandler(createSecurityHandler());
        ctx.setThrowUnavailableOnStartupException(true);

        return ctx;
    }

    private static Resource createResourceCollection(ContextHandler contextHandler) {
        var factory = ResourceFactory.of(contextHandler);
        return ResourceFactory.combine(factory.newClassLoaderResource("/META-INF/resources/webjars/", false),
            factory.newClassLoaderResource("/web", false));
    }

    private Integer getServerPort() {
        return this.serverPort;
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

    public static String getCookiePath() {
        var appname = ApplicationName.hvilkenTilbake();
        return switch (appname) {
            case FPTILBAKE -> null;
            case K9TILBAKE -> "/k9";
            default -> throw new IllegalArgumentException("Ikke-støttet applikasjonsnavn: " + appname);
        };
    }

    /*
     * K9-spesifikke ting som brukes ifm JASPI
     */
    /**
     * Legges først slik at alltid resetter context før prosesserer nye requests.
     * Kjøres først så ikke risikerer andre har satt Request#setHandled(true).
     */
    static final class ResetLogContextHandler extends Handler.Abstract {
        @Override
        public boolean handle(Request request, Response response, Callback callback) {
            MDC.clear();
            return false;
        }
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

    private void setContextAndCookiePath() {
        ContextPathHolder.instance(CONTEXT_PATH, "/k9");
    }

}
