package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.feil.FunksjonellFeil;

public class AktivKravgrunnlagAllerdeFinnesException extends FunksjonellException {
    public AktivKravgrunnlagAllerdeFinnesException(FunksjonellFeil feil) {
        super(feil);
    }
}
