package no.nav.foreldrepenger.tilbakekreving.kafka.poller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.apptjeneste.AppServiceHandler;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.util.Tuple;

@ApplicationScoped
public class KafkaPollerManager implements AppServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(KafkaPollerManager.class);

    private EntityManager entityManager;

    /**
     * Prefix every thread in pool with given name.
     */
    private final String threadPoolNamePrefix = getClass().getSimpleName();

    /**
     * Delay between each interval of polling. (millis)
     * <p>
     * kan ha veldig kort mellomrom mellom polls til Kafka siden:
     * 1. Ved lav frekvens på innkommende meldinger vil hyppighet på kall styres av parametret til poll i hver consumer
     * 2. Ved feil vil backoff-benyttes (se Poller)
     */
    private long delayBetweenPollingMillis = getSystemPropertyWithLowerBoundry("task.manager.polling.delay", 50L, 10L);

    /**
     * Single scheduled thread handling polling.
     */
    private Map<String, Tuple<KafkaPoller, ScheduledExecutorService>> pollingService;

    /**
     * Future for å kunne kansellere polling.
     */
    private Collection<ScheduledFuture<?>> pollingServiceScheduledFuture;
    private Instance<KafkaPoller> feedPollers;

    KafkaPollerManager() {
        //for CDI proxy
    }

    @Inject
    public KafkaPollerManager(@VLPersistenceUnit EntityManager entityManager, @Any Instance<KafkaPoller> feedPollers) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        Objects.requireNonNull(feedPollers, "feedPollers"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.feedPollers = feedPollers;
    }

    @Override
    public synchronized void start() {
        log.info("Lesing fra kafka startes nå");
        startPollerThread();
    }

    @Override
    public synchronized void stop() {
        if (pollingServiceScheduledFuture != null) {
            for (ScheduledFuture<?> scheduledFuture : pollingServiceScheduledFuture) {
                scheduledFuture.cancel(true);
            }
            pollingServiceScheduledFuture = null;
        }
    }

    private void startPollerThread() {
        if (pollingServiceScheduledFuture != null) {
            throw new IllegalStateException("Service allerede startet, stopp først");//$NON-NLS-1$
        }
        if (pollingService == null) {
            pollingService = new LinkedHashMap<>();
            for (KafkaPoller kafkaPoller : feedPollers) {
                String threadName = threadPoolNamePrefix + "-" + kafkaPoller.getName() + "-poller";
                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new PollerUtils.NamedThreadFactory(threadName));
                Tuple<KafkaPoller, ScheduledExecutorService> tuple = new Tuple<>(kafkaPoller, scheduledExecutorService);
                pollingService.put(kafkaPoller.getName(), tuple);
                log.info("Created thread for feed polling {}", threadName); // NOSONAR
            }
        }
        this.pollingServiceScheduledFuture = new ArrayList<>();
        for (Map.Entry<String, Tuple<KafkaPoller, ScheduledExecutorService>> entry : pollingService.entrySet()) {
            Tuple<KafkaPoller, ScheduledExecutorService> tuple = entry.getValue();
            KafkaPoller kafkaPoller = tuple.getElement1();
            ScheduledExecutorService service = tuple.getElement2();
            Poller poller = new Poller(entityManager, kafkaPoller);
            ScheduledFuture<?> scheduledFuture = service.scheduleWithFixedDelay(poller, delayBetweenPollingMillis / 2, delayBetweenPollingMillis, TimeUnit.MILLISECONDS);// NOSONAR
            pollingServiceScheduledFuture.add(scheduledFuture);
            log.debug("Lagt til ny poller til pollingtjeneste. poller={}, delayBetweenPollingMillis={}", kafkaPoller.getName(), delayBetweenPollingMillis);
        }
    }

    private static long getSystemPropertyWithLowerBoundry(String key, long defaultValue, long lowerBoundry) {
        final String property = System.getProperty(key, String.valueOf(defaultValue));
        final long systemPropertyValue = Long.parseLong(property);
        if (systemPropertyValue < lowerBoundry) {
            return lowerBoundry;
        }
        return systemPropertyValue;
    }
}
