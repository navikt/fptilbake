package no.nav.foreldrepenger.tilbakekreving.hendelser.felles;

import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;

public interface HendelseReader {

    PostTransactionHandler hentOgBehandleMeldinger();
}
