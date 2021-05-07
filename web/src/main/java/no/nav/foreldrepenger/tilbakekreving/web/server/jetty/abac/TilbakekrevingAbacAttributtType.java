package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;

public enum TilbakekrevingAbacAttributtType implements AbacAttributtType {

    YTELSEBEHANDLING_UUID("ytelsebehandling-uuid");

    private final String sporingsloggEksternKode;

    TilbakekrevingAbacAttributtType(String sporingsloggEksternKode) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
    }

    @Override
    public boolean getMaskerOutput() {
        return false;
    }

}
