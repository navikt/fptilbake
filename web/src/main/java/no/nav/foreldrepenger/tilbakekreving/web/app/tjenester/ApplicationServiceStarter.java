package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.hendelser.VedtakConsumer;
import no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer.KravgrunnlagAsyncJmsConsumer;
import no.nav.foreldrepenger.tilbakekreving.sensu.SensuKlient;
import no.nav.vedtak.felles.prosesstask.impl.BatchTaskScheduler;
import no.nav.vedtak.felles.prosesstask.impl.TaskManager;
import no.nav.vedtak.log.metrics.Controllable;

@ApplicationScoped
public class ApplicationServiceStarter {
    private static final Environment ENV = Environment.current();
    private static final Boolean MQ_DISABLED = ENV.getProperty("test.only.disable.mq", Boolean.class);
    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceStarter.class);
    private final List<Controllable> services = new ArrayList<>();

    public void startServices() {
        start(TaskManager.class);
        start(BatchTaskScheduler.class);
        start(VedtakConsumer.class);

        if (ApplicationName.hvilkenTilbake() == Fagsystem.K9TILBAKE) {
            start(SensuKlient.class);
        } else {
            logger.info("Starter ikke sensu klient");
        }

        // Startes alltid i prod og dev. Lokal og autotest styres med test.only.disable.mq flagg.
        if (!ENV.isLocal() || !Boolean.TRUE.equals(MQ_DISABLED)) {
            start(KravgrunnlagAsyncJmsConsumer.class);
        } else {
            logger.info("Startet IKKE QueueConsumerManager, den er disablet med test.only.disable.mq=true");
        }
    }

    public void stopServices() {
        services.forEach(this::stopp);
    }

    private void start(Class<? extends Controllable> controllable) {
        var service = CDI.current().select(controllable).get();
        if (services.contains(service)) {
            logger.warn("Starter ikke {} siden den allerede er startet", controllable.getSimpleName());
        } else {
            logger.info("Starter {}", controllable.getSimpleName());
            service.start();
            services.add(service);
        }
    }

    private void stopp(Controllable service) {
        logger.info("Stopper {}", service.getClass().getSimpleName());
        service.stop();
        services.remove(service);
    }
}
