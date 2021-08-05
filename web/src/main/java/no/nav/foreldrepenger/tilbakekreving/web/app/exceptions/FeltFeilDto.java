package no.nav.foreldrepenger.tilbakekreving.web.app.exceptions;

public record FeltFeilDto(String navn, String melding, String metainformasjon) {

    public FeltFeilDto(String navn, String melding) {
        this(navn, melding, null);
    }

}
