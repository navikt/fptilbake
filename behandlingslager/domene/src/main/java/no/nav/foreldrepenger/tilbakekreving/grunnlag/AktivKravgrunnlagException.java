package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.feil.FunksjonellFeil;

public class AktivKravgrunnlagException extends FunksjonellException {
    public AktivKravgrunnlagException(FunksjonellFeil feil) {
        super(feil);
    }
}
