package no.nav.foreldrepenger.tilbakekreving.web.app.startupinfo;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.NaisRestTjeneste;

@WebListener
public class AppStartupServletContextListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(AppStartupServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOG.info("Startup finished.");
        CDI.current().select(NaisRestTjeneste.class).get().setIsContextStartupReady(Boolean.TRUE);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // ikke noe
        LOG.info("Teardown finished.");
    }
}
