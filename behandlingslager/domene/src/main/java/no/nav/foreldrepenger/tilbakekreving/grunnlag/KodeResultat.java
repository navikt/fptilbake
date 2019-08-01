package no.nav.foreldrepenger.tilbakekreving.grunnlag;

public enum KodeResultat {

    FORELDET("FORELDET"),
    FEILREGISTRERT("FEILREGISTRERT"),
    INGEN_TILBAKEKREVING("INGEN_TILBAKEKREV"),
    DELVIS_TILBAKEKREVING("DELVIS_TILBAKEKREV"),
    FULL_TILBAKEKREVING("FULL_TILBAKEKREV");

    private String kode;

    KodeResultat(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
