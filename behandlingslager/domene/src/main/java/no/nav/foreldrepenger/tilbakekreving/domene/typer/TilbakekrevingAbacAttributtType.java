package no.nav.foreldrepenger.tilbakekreving.domene.typer;

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

    @Override
    public String getSporingsloggKode() {
        return sporingsloggEksternKode;
    }
}
