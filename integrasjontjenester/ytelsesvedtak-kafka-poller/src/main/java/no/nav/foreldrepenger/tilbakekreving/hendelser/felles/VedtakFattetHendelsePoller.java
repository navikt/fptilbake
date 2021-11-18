package no.nav.foreldrepenger.tilbakekreving.hendelser.felles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.tilbakekreving.hendelser.vedtakfattet.VedtakFattetReader;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.KafkaPoller;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;

@ApplicationScoped
public class VedtakFattetHendelsePoller implements KafkaPoller {

    private VedtakFattetReader hendelseReader;

    VedtakFattetHendelsePoller(){
        // for CDI
    }

    @Inject
    public VedtakFattetHendelsePoller(VedtakFattetReader hendelseReader) {
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
