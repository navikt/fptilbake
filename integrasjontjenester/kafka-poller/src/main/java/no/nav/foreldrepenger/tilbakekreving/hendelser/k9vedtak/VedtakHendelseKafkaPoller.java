package no.nav.foreldrepenger.tilbakekreving.hendelser.k9vedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.tilbakekreving.kafka.poller.KafkaPoller;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;

@ApplicationScoped
public class VedtakHendelseKafkaPoller implements KafkaPoller {

    private VedtakHendelseReader vedtakHendelseReader;

    VedtakHendelseKafkaPoller() {
        // CDI
    }

    @Inject
    public VedtakHendelseKafkaPoller(VedtakHendelseReader vedtakHendelseReader) {
        this.vedtakHendelseReader = vedtakHendelseReader;
    }

    @Override
    public String getName() {
        return FeedProperties.FEED_NAME;
    }

    @Timed
    @Override
    public PostTransactionHandler poll() {
        return vedtakHendelseReader.hentOgBehandleMeldinger();
    }
}
