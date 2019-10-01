package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

public enum Tillegsinformasjon {
    PERSONOPPLYSNINGER("soeker-personopplysninger"),
    VARSELTEKST("tilbakekrevingsvarsel-fritekst"),
    SØKNAD("soknad"),
    TILBAKEKREVINGSVALG("tilbakekreving-valg"),
    FAGSAK("fagsak");

    private String fpsakRelasjonNavn;

    Tillegsinformasjon(String fpsakRelasjonNavn) {
        this.fpsakRelasjonNavn = fpsakRelasjonNavn;
    }

    public String getFpsakRelasjonNavn() {
        return fpsakRelasjonNavn;
    }
}
