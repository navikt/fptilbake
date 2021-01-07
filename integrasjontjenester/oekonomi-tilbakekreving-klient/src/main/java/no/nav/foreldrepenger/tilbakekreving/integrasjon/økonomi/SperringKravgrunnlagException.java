package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class SperringKravgrunnlagException extends IntegrasjonException {
    public SperringKravgrunnlagException(Feil feil) {
        super(feil);
    }
}
