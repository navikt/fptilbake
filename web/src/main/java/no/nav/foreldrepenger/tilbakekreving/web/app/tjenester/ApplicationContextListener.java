package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationContextListener.class);

    @Inject
    private ApplicationServiceStarter applicationServiceStarter; //NOSONAR - vil ikke fungere med constructor innjection

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("Start services.");
        applicationServiceStarter.startServices();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        applicationServiceStarter.stopServices();
        LOG.info("Stop services.");
    }

}
