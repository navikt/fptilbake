package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.hotspot.DefaultExports;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.KafkaPollerManager;
import no.nav.vedtak.apptjeneste.AppServiceHandler;
import no.nav.vedtak.felles.integrasjon.jms.QueueConsumerManager;
import no.nav.vedtak.felles.prosesstask.impl.TaskManager;
import no.nav.vedtak.felles.prosesstask.impl.cron.BatchTaskScheduler;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class ApplicationServiceStarterImpl implements ApplicationServiceStarter {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceStarterImpl.class);
    private List<Class<AppServiceHandler>> services = new ArrayList<>();

    @Override
    public void startServices() {
        DefaultExports.initialize();

        start(TaskManager.class);
        start(BatchTaskScheduler.class);
        start(KafkaPollerManager.class);
        if (Environment.current().isProd() || !"true".equalsIgnoreCase(Environment.current().getProperty("test.only.disable.mq"))) {
            start(QueueConsumerManager.class);
        } else {
            logger.info("Startet IKKE QueueConsumerManager, den er disablet med test.only.disable.mq=true");
        }
    }

    @Override
    public void stopServices() {
        for (Class<AppServiceHandler> serviceClass : services) {
            stopp(serviceClass);
        }
    }

    private void start(Class<? extends AppServiceHandler> klasse) {
        if (services.contains(klasse)) {
            logger.warn("Starter ikke {} siden den allerede er startet", klasse);
        } else {
            logger.info("Starter {}", klasse.getSimpleName());
            CDI.current().select(klasse).get().start();
        }
    }

    private void stopp(Class<? extends AppServiceHandler> klasse) {
        logger.info("Stopper {}", klasse.getSimpleName());
        CDI.current().select(klasse).get().stop();
    }

}
