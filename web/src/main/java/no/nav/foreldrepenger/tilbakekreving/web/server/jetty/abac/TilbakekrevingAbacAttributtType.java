package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;

public enum TilbakekrevingAbacAttributtType implements AbacAttributtType {

    YTELSEBEHANDLING_UUID;

    @Override
    public boolean getMaskerOutput() {
        return false;
    }

}
