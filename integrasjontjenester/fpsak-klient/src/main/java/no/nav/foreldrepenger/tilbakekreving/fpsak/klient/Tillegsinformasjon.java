package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

public enum Tillegsinformasjon {
    PERSONOPPLYSNINGER("soeker-personopplysninger"),
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
