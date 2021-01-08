package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class ØkonomiConsumerProducerDelegator {

    private ØkonomiConsumerProducer producer;

    @Inject
    public ØkonomiConsumerProducerDelegator(ØkonomiConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public ØkonomiConsumer økonomiConsumerForEndUser() {
        return producer.økonomiConsumer();
    }

    @Produces
    public ØkonomiSelftestConsumer økonomiSelftestConsumerForSystemUser() {
        return producer.økonomiSelftestConsumer();
    }
}
