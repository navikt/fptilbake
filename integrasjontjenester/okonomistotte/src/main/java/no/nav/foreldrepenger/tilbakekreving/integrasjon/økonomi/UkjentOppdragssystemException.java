package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class UkjentOppdragssystemException extends IntegrasjonException {
    public UkjentOppdragssystemException(Feil feil) {
        super(feil);
    }
}
