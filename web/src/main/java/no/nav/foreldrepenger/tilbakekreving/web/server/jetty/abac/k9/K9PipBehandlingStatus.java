package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.K9RessursDataValue;

public enum K9PipBehandlingStatus implements K9RessursDataValue {
    OPPRETTET("Opprettet"),
    UTREDES("Behandling utredes"),
    FATTE_VEDTAK("Kontroller og fatte vedtak");

    private final String verdi;

    K9PipBehandlingStatus(String verdi) {
        this.verdi = verdi;
    }

    @Override
    public String getVerdi() {
        return verdi;
    }
}
