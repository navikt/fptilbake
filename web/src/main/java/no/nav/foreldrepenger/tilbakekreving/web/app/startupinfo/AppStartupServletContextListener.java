package no.nav.foreldrepenger.tilbakekreving.web.app.startupinfo;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.NaisRestTjeneste;

public class AppStartupServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        CDI.current().select(NaisRestTjeneste.class).get().setIsContextStartupReady(Boolean.TRUE);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // ikke noe
    }
}
