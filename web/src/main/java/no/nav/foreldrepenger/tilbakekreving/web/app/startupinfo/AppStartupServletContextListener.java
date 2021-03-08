package no.nav.foreldrepenger.tilbakekreving.web.app.startupinfo;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppStartupServletContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppStartupServletContextListener.class);

    @Inject
    private AppStartupInfoLogger appStartupInfoLogger; // NOSONAR

    // for enhetstest
    void setAppStartupInfoLogger(AppStartupInfoLogger appStartupInfoLogger) {
        this.appStartupInfoLogger = appStartupInfoLogger;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            appStartupInfoLogger.logAppStartupInfo();
        } catch (Exception e) {
            logger.error("FPT-753407: Uventet exception ved oppstart", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // ikke noe
    }
}
