package no.nav.foreldrepenger.tilbakekreving.sokos;

import no.nav.vedtak.exception.IntegrasjonException;

public class SokosKravgrunnlagException extends IntegrasjonException {

    private final String melding;

    public SokosKravgrunnlagException(String melding) {
        super("F-468817", "Sokos avviste henting av kravgrunnlag.");
        this.melding = melding;
    }

    public String melding() {
        return melding;
    }
}
