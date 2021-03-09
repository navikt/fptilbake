package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.message.config.AuthConfigFactory;

import org.apache.geronimo.components.jaspi.AuthConfigFactoryImpl;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.jaspi.JaspiAuthenticatorFactory;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.slf4j.bridge.SLF4JBridgeHandler;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.SecurityFilter;

abstract class AbstractJettyServer {

    /**
     * @see AbstractNetworkConnector#getHost()
     * @see ServerConnector#openAcceptChannel()
     */
    //TODO (u139158): Trenger vi egentlig å sette denne? Spec ser ut til å si at det er eq med null, settes den default til null eller binder den mot et interface?

    protected static final String SERVER_HOST = "0.0.0.0";
    /**
     * nedstrippet sett med Jetty configurations for raskere startup.
     */
    protected static final Configuration[] CONFIGURATIONS = new Configuration[]{
        new WebAppConfiguration(),
        new WebInfConfiguration(),
        new WebXmlConfiguration(),
        new AnnotationConfiguration(),
        new EnvConfiguration(),
        new PlusConfiguration(),
    };
    private AppKonfigurasjon appKonfigurasjon;

    public AbstractJettyServer(AppKonfigurasjon appKonfigurasjon) {
        this.appKonfigurasjon = appKonfigurasjon;
    }


    protected void bootStrap() throws Exception {
        konfigurer();
        migrerDatabaser();
        start(appKonfigurasjon);
    }

    protected void konfigurer() throws Exception {
        konfigurerLogging();
        konfigurerMiljø();
        konfigurerSikkerhet();
        konfigurerJndi();
    }

    private void konfigurerLogging() {
        //openhtmltopdf bruker java.util.logging og konfigurerer egen handler, overstyrer vha system property
        System.setProperty("xr.util-logging.handlers", "org.slf4j.bridge.SLF4JBridgeHandler");
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    protected abstract void konfigurerMiljø() throws Exception;

    protected void konfigurerSikkerhet() {
        Security.setProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY, AuthConfigFactoryImpl.class.getCanonicalName());

        File jaspiConf = new File(System.getProperty("conf", "./conf") + "/jaspi-conf.xml");
        if (!jaspiConf.exists()) {
            throw new IllegalStateException("Missing required file: " + jaspiConf.getAbsolutePath());
        }
        System.setProperty("org.apache.geronimo.jaspic.configurationFile", jaspiConf.getAbsolutePath());

        konfigurerSwaggerHash();
    }

    /**
     * @see SecurityFilter#getSwaggerHash()
     */
    protected void konfigurerSwaggerHash() {
        System.setProperty(SecurityFilter.SWAGGER_HASH_KEY, appKonfigurasjon.getSwaggerHash());
    }

    protected abstract void konfigurerJndi() throws Exception;

    protected abstract void migrerDatabaser() throws IOException;

    protected void start(AppKonfigurasjon appKonfigurasjon) throws Exception {
        Server server = new Server(appKonfigurasjon.getServerPort());
        server.setConnectors(createConnectors(appKonfigurasjon, server).toArray(new Connector[]{}));

        RewriteHandler rewriteHandler = createRewriteHandler();
        WebAppContext context = createContext(appKonfigurasjon);
        rewriteHandler.setHandler(context);

        server.setHandler(rewriteHandler);
        server.start();
        server.join();
    }

    @SuppressWarnings("resource")
    protected List<Connector> createConnectors(AppKonfigurasjon appKonfigurasjon, Server server) {
        List<Connector> connectors = new ArrayList<>();
        ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(createHttpConfiguration()));
        httpConnector.setPort(appKonfigurasjon.getServerPort());
        httpConnector.setHost(SERVER_HOST);
        connectors.add(httpConnector);

        return connectors;
    }

    private RewriteHandler createRewriteHandler() {
        RewriteHandler rewriteHandler = new RewriteHandler();
        rewriteHandler.setRewriteRequestURI(true);
        rewriteHandler.setRewritePathInfo(false);
        rewriteHandler.setOriginalPathAttribute("requestedPath");

        return rewriteHandler;
    }

    protected WebAppContext createContext(AppKonfigurasjon appKonfigurasjon) throws IOException {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setParentLoaderPriority(true);

        // må hoppe litt bukk for å hente web.xml fra classpath i stedet for fra filsystem.
        String descriptor = Resource.newClassPathResource("/WEB-INF/web.xml").getURI().toURL().toExternalForm();
        webAppContext.setDescriptor(descriptor);
        webAppContext.setBaseResource(createResourceCollection());
        webAppContext.setContextPath(appKonfigurasjon.getContextPath());
        webAppContext.setConfigurations(CONFIGURATIONS);
        webAppContext.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", "^.*resteasy-.*.jar$|^.*felles-.*.jar$");
        webAppContext.setSecurityHandler(createSecurityHandler());

        return webAppContext;
    }


    protected HttpConfiguration createHttpConfiguration() {
        // Create HTTP Config
        HttpConfiguration httpConfig = new HttpConfiguration();

        // Add support for X-Forwarded headers
        httpConfig.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());

        return httpConfig;

    }

    protected abstract Resource createResourceCollection() throws IOException;

    private SecurityHandler createSecurityHandler() {
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticatorFactory(new JaspiAuthenticatorFactory());

        JAASLoginService loginService = new JAASLoginService();
        loginService.setName("jetty-login");
        loginService.setLoginModuleName("jetty-login");
        loginService.setIdentityService(new DefaultIdentityService());
        securityHandler.setLoginService(loginService);

        return securityHandler;
    }

}
