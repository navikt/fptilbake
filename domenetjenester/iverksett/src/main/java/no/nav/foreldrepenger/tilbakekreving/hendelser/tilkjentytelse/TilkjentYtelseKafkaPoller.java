package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.tilbakekreving.kafka.poller.KafkaPoller;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;

@ApplicationScoped
public class TilkjentYtelseKafkaPoller implements KafkaPoller {

    private TilkjentYtelseReader tilkjentYtelseReader;

    TilkjentYtelseKafkaPoller() {
        // CDI
    }

    @Inject
    public TilkjentYtelseKafkaPoller(TilkjentYtelseReader tilkjentYtelseReader) {
        this.tilkjentYtelseReader = tilkjentYtelseReader;
    }

    @Override
    public String getName() {
        return FeedProperties.FEED_NAME;
    }

    @Timed
    @Override
    public PostTransactionHandler poll() {
        return tilkjentYtelseReader.hentOgBehandleMeldinger();
    }
}
