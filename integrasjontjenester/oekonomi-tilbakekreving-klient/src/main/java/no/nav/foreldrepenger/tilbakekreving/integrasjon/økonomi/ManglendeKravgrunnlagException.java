package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class ManglendeKravgrunnlagException extends IntegrasjonException {
    public ManglendeKravgrunnlagException(Feil feil) {
        super(feil);
    }
}
