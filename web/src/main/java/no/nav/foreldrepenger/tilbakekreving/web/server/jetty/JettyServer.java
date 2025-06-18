package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import static org.eclipse.jetty.ee10.webapp.MetaInfConfiguration.CONTAINER_JAR_PATTERN;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.security.auth.message.config.AuthConfigFactory;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.ContextPathHolder;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.jaspic.OidcAuthModule;

import org.eclipse.jetty.ee10.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee10.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee10.security.jaspi.DefaultAuthConfigFactory;
import org.eclipse.jetty.ee10.security.jaspi.JaspiAuthenticatorFactory;
import org.eclipse.jetty.ee10.security.jaspi.provider.JaspiAuthConfigProvider;
import org.eclipse.jetty.ee10.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.Constraint;
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
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.ApiConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.InternalApiConfig;

public class JettyServer {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private static final String CONTEXT_PATH = getContextPath();
    private static final String JETTY_SCAN_LOCATIONS = "^.*jersey-.*\\.jar$|^.*felles-.*\\.jar$|^.*/app\\.jar$";
    private static final String JETTY_LOCAL_CLASSES = "^.*/target/classes/|";


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
        ApplicationName.hvilkenTilbake(); // Sørger for at den initialiseres fra environment
        if (Fagsystem.K9TILBAKE.equals(ApplicationName.hvilkenTilbake())) {
            setContextAndCookiePath();
        }
    }

    protected void bootStrap() throws Exception {
        konfigurerLogging();
        konfigurerSikkerhet();
        var dataSource = DatasourceUtil.createDatasource(30, 2);
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
        if (Fagsystem.K9TILBAKE.equals(ApplicationName.hvilkenTilbake())) {
            var factory = new DefaultAuthConfigFactory();
            factory.registerConfigProvider(new JaspiAuthConfigProvider(new OidcAuthModule()),
                "HttpServlet",
                "server " + CONTEXT_PATH,
                "OIDC Authentication");

            AuthConfigFactory.setFactory(factory);
        }
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
        if (Fagsystem.K9TILBAKE.equals(ApplicationName.hvilkenTilbake())) {
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
        var ctx = new WebAppContext(CONTEXT_PATH, null, simpleConstraints(), null,
            new ErrorPageErrorHandler(), ServletContextHandler.NO_SESSIONS);
        ctx.setParentLoaderPriority(true);
        // må hoppe litt bukk for å hente web.xml fra classpath i stedet for fra filsystem.
        String baseResource;
        try (var factory = ResourceFactory.closeable()) {
            baseResource = factory.newResource(".").getRealURI().toURL().toExternalForm();
        }

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

        ctx.setThrowUnavailableOnStartupException(true);

        return ctx;
    }

    private static Resource createResourceCollection(ContextHandler contextHandler) {
        var factory = ResourceFactory.of(contextHandler);
        return ResourceFactory.combine(factory.newClassLoaderResource("/META-INF/resources/webjars/", false),
            factory.newClassLoaderResource("/web", false));
    }

    private static ConstraintSecurityHandler simpleConstraints() {
        var handler = new ConstraintSecurityHandler();
        // Slipp gjennom kall fra plattform til JaxRs. Foreløpig kun behov for GET
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, InternalApiConfig.API_URI + "/*"));
        // Slipp gjennom til autentisering i JaxRs / auth-filter
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, ApiConfig.API_URI + "/*"));
        // K9-tilbake bruker deprekert swagger-oppsett
        if (Fagsystem.K9TILBAKE.equals(ApplicationName.hvilkenTilbake())) {
            handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, "/swagger-ui/*"));
            handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, "/swagger/*"));
        }
        // Alt annet av paths og metoder forbudt - 403
        handler.addConstraintMapping(pathConstraint(Constraint.FORBIDDEN, "/*"));
        return handler;
    }

    private static ConstraintMapping pathConstraint(Constraint constraint, String path) {
        var mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec(path);
        return mapping;
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
