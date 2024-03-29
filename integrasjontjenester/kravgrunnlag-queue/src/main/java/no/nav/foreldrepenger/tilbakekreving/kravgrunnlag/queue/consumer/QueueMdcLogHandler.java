package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

import no.nav.foreldrepenger.felles.jms.MdcHandler;
import no.nav.vedtak.log.mdc.MDCOperations;

class QueueMdcLogHandler implements MdcHandler {
    @Override
    public void setCallId(String callId) {
        MDCOperations.putCallId(callId);
    }

    @Override
    public void settNyCallId() {
        MDCOperations.putCallId();
    }

    @Override
    public void removeCallId() {
        MDCOperations.removeCallId();
    }
}
