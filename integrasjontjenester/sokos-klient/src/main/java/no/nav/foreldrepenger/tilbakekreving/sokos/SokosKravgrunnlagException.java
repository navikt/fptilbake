package no.nav.foreldrepenger.tilbakekreving.sokos;

import java.net.HttpURLConnection;

import no.nav.vedtak.exception.IntegrasjonException;

public class SokosKravgrunnlagException extends IntegrasjonException {

    private final String melding;

    public SokosKravgrunnlagException(String melding) {
        super("F-468817", "Sokos avviste henting av kravgrunnlag: " + melding, HttpURLConnection.HTTP_BAD_REQUEST);
        this.melding = melding;
    }

    public String melding() {
        return melding;
    }
}
