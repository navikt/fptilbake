package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient;

public enum Tillegsinformasjon {
    PERSONOPPLYSNINGER("personopplysninger-tilbake", "soeker-personopplysninger"),
    VARSELTEKST("tilbakekrevingsvarsel-fritekst"),
    SØKNAD("soknad"),
    TILBAKEKREVINGSVALG("tilbakekreving-valg", "tilbakekrevingvalg"),
    FAGSAK("fagsak-backend", "fagsak"),
    VERGE("verge-backend", "verge");

    private String fpsakRelasjonNavn;
    private String k9sakRelasjonNavn;

    Tillegsinformasjon(String fpsakRelasjonNavn) {
        this.fpsakRelasjonNavn = fpsakRelasjonNavn;
    }

    Tillegsinformasjon(String fpsakRelasjonNavn, String k9sakRelasjonNavn) {
        this.fpsakRelasjonNavn = fpsakRelasjonNavn;
        this.k9sakRelasjonNavn = k9sakRelasjonNavn;
    }

    public String getFpsakRelasjonNavn() {
        return fpsakRelasjonNavn;
    }

    public String getK9sakRelasjonNavn() {
        return k9sakRelasjonNavn != null ? k9sakRelasjonNavn : fpsakRelasjonNavn;
    }
}
