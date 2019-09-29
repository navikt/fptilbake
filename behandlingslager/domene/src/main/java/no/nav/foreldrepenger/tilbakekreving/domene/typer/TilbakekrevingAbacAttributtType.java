package no.nav.foreldrepenger.tilbakekreving.domene.typer;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;

public enum TilbakekrevingAbacAttributtType implements AbacAttributtType {

    FPSAK_BEHANDLING_UUID("fpsak-behandling-uuid");

    private final String sporingsloggEksternKode;
    private final boolean maskerOutput;
    private final boolean valider;

    TilbakekrevingAbacAttributtType(String sporingsloggEksternKode) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = false;
        valider = false;
    }

    TilbakekrevingAbacAttributtType(String sporingsloggEksternKode, boolean maskerOutput, boolean valider) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = maskerOutput;
        this.valider = valider;
    }

    @Override
    public String getSporingsloggEksternKode() {
        return sporingsloggEksternKode;
    }

    @Override
    public boolean getMaskerOutput() {
        return maskerOutput;
    }

    @Override
    public boolean getValider() {
        return valider;
    }

}
