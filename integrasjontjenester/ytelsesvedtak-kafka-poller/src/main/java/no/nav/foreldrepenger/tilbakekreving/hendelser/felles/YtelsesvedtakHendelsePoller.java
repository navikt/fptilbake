package no.nav.foreldrepenger.tilbakekreving.hendelser.felles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.tilbakekreving.kafka.poller.KafkaPoller;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;

@ApplicationScoped
public class YtelsesvedtakHendelsePoller implements KafkaPoller {

    private HendelseReader hendelseReader;

    YtelsesvedtakHendelsePoller(){
        // for CDI
    }

    @Inject
    public YtelsesvedtakHendelsePoller(HendelseReader hendelseReader) {
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
