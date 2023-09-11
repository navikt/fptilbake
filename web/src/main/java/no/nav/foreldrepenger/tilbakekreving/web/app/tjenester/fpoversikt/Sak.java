package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fpoversikt;

import java.time.LocalDateTime;

public record Sak(String saksnummer, Varsel varsel, boolean harVerge) {

    public record Varsel(LocalDateTime utsendtTidspunkt, boolean besvart) {
    }
}
