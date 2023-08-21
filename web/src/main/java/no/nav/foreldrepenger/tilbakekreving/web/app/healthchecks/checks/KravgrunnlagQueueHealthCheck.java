package no.nav.foreldrepenger.tilbakekreving.web.app.healthchecks.checks;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import no.nav.foreldrepenger.felles.jms.QueueSelftest;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer.KravgrunnlagAsyncJmsConsumer;
import no.nav.vedtak.log.metrics.ReadinessAware;

@ApplicationScoped
public class KravgrunnlagQueueHealthCheck implements ReadinessAware {
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
        if (ApplicationName.hvilkenTilbake() == Fagsystem.K9TILBAKE){
            //ignorerer sjekken. sjekken tar ofte lang tid, som skaper støy i overvåkningen.
            return true;
        }
        try {
            client.testConnection();
        } catch (JMSRuntimeException | JMSException e) { //NOSONAR
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
}
