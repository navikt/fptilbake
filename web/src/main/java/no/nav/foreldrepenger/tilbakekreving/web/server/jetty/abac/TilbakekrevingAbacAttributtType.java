package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;

public enum TilbakekrevingAbacAttributtType implements AbacAttributtType {

    BEHANDLING_ID,
    YTELSEBEHANDLING_UUID;

    @Override
    public boolean getMaskerOutput() {
        return false;
    }

}
