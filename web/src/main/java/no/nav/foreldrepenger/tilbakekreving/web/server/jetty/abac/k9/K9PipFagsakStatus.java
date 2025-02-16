package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.K9RessursDataValue;

public enum K9PipFagsakStatus implements K9RessursDataValue {
    OPPRETTET("Opprettet"),
    UNDER_BEHANDLING("Under behandling");

    private final String verdi;

    K9PipFagsakStatus(String verdi) {
        this.verdi = verdi;
    }

    @Override
    public String getVerdi() {
        return verdi;
    }
}
