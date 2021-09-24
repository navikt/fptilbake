package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.web.app.ApplicationConfig;
import no.nav.vedtak.isso.IssoApplication;

public class JettyServer extends AbstractJettyServer {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private DataSourceKonfig dataSourceKonfig;

    public JettyServer() {
        this(new JettyWebKonfigurasjon());
    }

    public JettyServer(int serverPort) {
        this(new JettyWebKonfigurasjon(serverPort));
    }

    JettyServer(AppKonfigurasjon appKonfigurasjon) {
        super(appKonfigurasjon);
    }

    public static void main(String[] args) throws Exception {
        JettyServer jettyServer;
        if (args.length > 0) {
            int serverPort = Integer.parseUnsignedInt(args[0]);
            jettyServer = new JettyServer(serverPort);
        } else {
            jettyServer = new JettyServer();
        }
        jettyServer.bootStrap();
    }

    @Override
    protected void konfigurerMiljø() throws Exception {
        dataSourceKonfig = new DataSourceKonfig();
        hacks4Nais();
    }

    private void hacks4Nais() {
        temporært();
    }

    private void temporært() {
        // FIXME: PFP-1176 Skriv om i OpenAmIssoHealthCheck og AuthorizationRequestBuilder når Jboss dør
        if (System.getenv("OIDC_OPENAM_HOSTURL") != null) {
            LOG.info("Trickser med OIDC_OPENAM_HOSTURL");
            System.setProperty("OpenIdConnect.issoHost", System.getenv("OIDC_OPENAM_HOSTURL"));
        }
        // FIXME: PFP-1176 Skriv om i AuthorizationRequestBuilder og IdTokenAndRefreshTokenProvider når Jboss dør
        if (System.getenv("OIDC_OPENAM_AGENTNAME") != null) {
            LOG.info("Trickser med OIDC_OPENAM_AGENTNAME");
            System.setProperty("OpenIdConnect.username", System.getenv("OIDC_OPENAM_AGENTNAME"));
        }
        // FIXME: PFP-1176 Skriv om i IdTokenAndRefreshTokenProvider når Jboss dør
        if (System.getenv("OIDC_OPENAM_PASSWORD") != null) {
            LOG.info("Trickser med OIDC_OPENAM_PASSWORD");
            System.setProperty("OpenIdConnect.password", System.getenv("OIDC_OPENAM_PASSWORD"));
        }
    }

    @Override
    protected void konfigurerJndi() throws Exception {
        new EnvEntry("jdbc/defaultDS", dataSourceKonfig.getDefaultDatasource().getDatasource());
        konfigurerJms();
    }

    protected void konfigurerJms() throws JMSException, NamingException {
        JmsKonfig.settOppJndiConnectionfactory("jms/ConnectionFactory");
        JmsKonfig.settOppJndiMessageQueue("jms/QueueFptilbakeKravgrunnlag", "fptilbake_kravgrunnlag.queueName");
    }

    @Override
    protected void migrerDatabaser() throws IOException {
        for (DataSourceKonfig.DBConnProp dbConnProp : dataSourceKonfig.getDataSources()) {
            new DatabaseScript(dbConnProp.getDatasource(), dbConnProp.getMigrationScripts()).migrate();
        }
    }

    @Override
    protected WebAppContext createContext(AppKonfigurasjon appKonfigurasjon) throws IOException {
        WebAppContext webAppContext = super.createContext(appKonfigurasjon);
        webAppContext.setParentLoaderPriority(true);
        updateMetaData(webAppContext.getMetaData());
        return webAppContext;
    }

    private void updateMetaData(MetaData metaData) {
        // Find path to class-files while starting jetty from development environment.
        List<Class<?>> appClasses = List.of(ApplicationConfig.class, IssoApplication.class);

        List<Resource> resources = appClasses.stream()
            .map(c -> Resource.newResource(c.getProtectionDomain().getCodeSource().getLocation()))
            .distinct()
            .collect(Collectors.toList());

        metaData.setWebInfClassesResources(resources);
    }

    @Override
    protected ResourceCollection createResourceCollection() throws IOException {
        return new ResourceCollection(
            Resource.newClassPathResource("/META-INF/resources/webjars/"),
            Resource.newClassPathResource("/web"));
    }

}
