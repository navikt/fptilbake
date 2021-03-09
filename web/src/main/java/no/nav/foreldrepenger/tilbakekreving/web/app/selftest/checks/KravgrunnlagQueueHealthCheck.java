package no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer.KravgrunnlagAsyncJmsConsumer;

@ApplicationScoped
public class KravgrunnlagQueueHealthCheck extends QueueHealthCheck {

    KravgrunnlagQueueHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public KravgrunnlagQueueHealthCheck(KravgrunnlagAsyncJmsConsumer client) {
        super(client);
    }

    @Override
    public String getDescriptionSuffix() {
        return "Kravgrunnlag";
    }
}
