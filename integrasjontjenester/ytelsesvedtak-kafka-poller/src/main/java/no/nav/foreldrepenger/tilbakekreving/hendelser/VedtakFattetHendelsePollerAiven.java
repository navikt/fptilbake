package no.nav.foreldrepenger.tilbakekreving.hendelser;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.tilbakekreving.kafka.poller.KafkaPoller;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;

@ApplicationScoped
public class VedtakFattetHendelsePollerAiven implements KafkaPoller {

    private VedtakFattetReaderAiven hendelseReader;

    VedtakFattetHendelsePollerAiven() {
        // for CDI
    }

    @Inject
    public VedtakFattetHendelsePollerAiven(VedtakFattetReaderAiven hendelseReader) {
        this.hendelseReader = hendelseReader;
    }

    @Override
    public String getName() {
        return "Poller for " + hendelseReader.toString();
    }

    @Timed
    @Override
    public PostTransactionHandler poll() {
        return hendelseReader.hentOgBehandleMeldinger();
    }
}
