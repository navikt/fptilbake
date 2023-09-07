package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fpoversikt;

public record Sak(String saksnummer, Varsel varsel, boolean harVerge) {

    public record Varsel(boolean sendt, boolean besvart) {
    }
}
