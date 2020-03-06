package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto;

public class BaseDokument {

    private String fagsaktypeNavn;
    private boolean foreldrepenger;
    private boolean engangsstønad;
    private boolean svangerskapspenger;
    private boolean isKorrigert;

    public String getFagsaktypeNavn() {
        return fagsaktypeNavn;
    }

    public void setFagsaktypeNavn(String fagsaktypeNavn) {
        this.fagsaktypeNavn = fagsaktypeNavn;
    }

    public boolean isForeldrepenger() {
        return foreldrepenger;
    }

    public void setForeldrepenger(boolean foreldrepenger) {
        this.foreldrepenger = foreldrepenger;
    }

    public boolean isEngangsstønad() {
        return engangsstønad;
    }

    public void setEngangsstønad(boolean engangsstønad) {
        this.engangsstønad = engangsstønad;
    }

    public boolean isSvangerskapspenger() {
        return svangerskapspenger;
    }

    public void setSvangerskapspenger(boolean svangerskapspenger) {
        this.svangerskapspenger = svangerskapspenger;
    }

    public boolean isKorrigert() {
        return isKorrigert;
    }

    public void setKorrigert(boolean korrigert) {
        isKorrigert = korrigert;
    }
}
