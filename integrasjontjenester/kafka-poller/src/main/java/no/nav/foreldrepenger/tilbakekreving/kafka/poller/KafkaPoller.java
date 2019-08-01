package no.nav.foreldrepenger.tilbakekreving.kafka.poller;

public interface KafkaPoller {

    String getName();

    PostTransactionHandler poll();
}
