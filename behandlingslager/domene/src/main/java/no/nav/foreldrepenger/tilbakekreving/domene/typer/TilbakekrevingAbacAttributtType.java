package no.nav.foreldrepenger.tilbakekreving.domene.typer;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;

public enum TilbakekrevingAbacAttributtType implements AbacAttributtType {

    FPSAK_BEHANDLING_UUID("fpsak-behandling-uuid");

    private final String sporingsloggEksternKode;
    private final boolean maskerOutput;

    TilbakekrevingAbacAttributtType(String sporingsloggEksternKode) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = false;
    }

    TilbakekrevingAbacAttributtType(String sporingsloggEksternKode, boolean maskerOutput) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = maskerOutput;
    }

    @Override
    public boolean getMaskerOutput() {
        return maskerOutput;
    }

    @Override
    public String getSporingsloggKode() {
        return sporingsloggEksternKode;
    }
}
