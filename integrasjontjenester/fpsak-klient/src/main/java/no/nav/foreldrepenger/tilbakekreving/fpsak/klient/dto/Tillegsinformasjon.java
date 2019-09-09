package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

public enum Tillegsinformasjon {
    PERSONOPPLYSNINGER("soeker-personopplysninger"),
    VARSELTEKST("tilbakekrevingsvarsel-fritekst"),
    TILBAKEKREVINGSVALG("tilbakekreving-valg");

    private String fpsakRelasjonNavn;

    Tillegsinformasjon(String fpsakRelasjonNavn) {
        this.fpsakRelasjonNavn = fpsakRelasjonNavn;
    }

    public String getFpsakRelasjonNavn() {
        return fpsakRelasjonNavn;
    }
}
