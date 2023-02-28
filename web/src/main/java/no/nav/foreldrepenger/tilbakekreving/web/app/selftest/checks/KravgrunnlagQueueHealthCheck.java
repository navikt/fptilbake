package no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import no.nav.foreldrepenger.felles.jms.QueueSelftest;
import no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer.KravgrunnlagAsyncJmsConsumer;

import no.nav.vedtak.log.metrics.LiveAndReadinessAware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class KravgrunnlagQueueHealthCheck implements LiveAndReadinessAware {
    private static final Logger LOG = LoggerFactory.getLogger(KravgrunnlagQueueHealthCheck.class);
    private QueueSelftest client;

    KravgrunnlagQueueHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public KravgrunnlagQueueHealthCheck(KravgrunnlagAsyncJmsConsumer kravgrunnlagAsyncJmsConsumer) {
        this.client = kravgrunnlagAsyncJmsConsumer;
    }

    private boolean isOK() {
        try {
            client.testConnection();
        } catch (JMSRuntimeException | JMSException e) { // NOSONAR
            if (LOG.isWarnEnabled()) {
                LOG.warn("Feil ved Kravgrunnlag meldingskø helsesjekk: {}", client.getConnectionEndpoint());
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isReady() {
        return isOK();
    }

    @Override
    public boolean isAlive() {
        return isOK();
    }
}
