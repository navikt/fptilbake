package no.nav.foreldrepenger.tilbakekreving.kafka.poller;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.kafka.RequestContextHandler;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.jpa.TransactionHandler;

public class Poller implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Poller.class);
    private static final int[] BACKOFF_INTERVALL_IN_SEC = new int[]{1, 2, 5, 5, 10, 10, 10, 10, 30};

    private EntityManager entityManager;
    private KafkaPoller kafkaPoller;

    private final AtomicInteger backoffRound = new AtomicInteger();

    Poller() {
        //for CDI proxy
    }

    Poller(EntityManager entityManager, KafkaPoller kafkaPoller) {
        this.entityManager = entityManager;
        this.kafkaPoller = kafkaPoller;
    }

    @Override
    public void run() {
        try {
            RequestContextHandler.doWithRequestContext(this::doPollWithEntityManager);
        } catch (VLException e) { //NOSONAR
            logger.warn(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Uventet feil i polling", e);
        }
    }

    private Void doPollWithEntityManager() {
        try {
            if (backoffRound.get() > 0) {
                Thread.sleep(BACKOFF_INTERVALL_IN_SEC[Math.min(backoffRound.get(), BACKOFF_INTERVALL_IN_SEC.length) - 1] * 1000L);
            }
            PostTransactionHandler postTransactionHandler = new PollInNewTransaction().doWork();
            postTransactionHandler.doAfterTransaction();
            backoffRound.set(0);

            return null;
        } catch (Exception e) {
            backoffRound.incrementAndGet();
            throw new TekniskException("FP-852160", String.format("Kunne ikke polle kafka hendelser, venter til neste runde(runde=%s)", backoffRound.get()), e);
        }
    }

    private final class PollInNewTransaction extends TransactionHandler<PostTransactionHandler> {

        PostTransactionHandler doWork() throws Exception {
            try {
                return super.apply(entityManager);
            } finally {
                CDI.current().destroy(entityManager);
            }
        }

        private PostTransactionHandler doPoll() {
            try {
                return kafkaPoller.poll();
            } finally {
                CDI.current().destroy(entityManager);
            }
        }

        @Override
        protected PostTransactionHandler doWork(EntityManager entityManager) {
            return doPoll();
        }
    }
}
