package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars;

public class BaseDokument {

    private String fagsaktypeNavn;
    private boolean foreldrepenger;
    private boolean engangsstonad;
    private boolean svangerskapspenger;

    public enum Lokale {
        BOKMÅL("nb_NO"),
        NYNORSK("nn_NO"),
        ENGELSK("en_GB");

        private final String tekst;

        Lokale(final String tekst) {
            this.tekst = tekst;
        }

        @Override
        public String toString() {
            return tekst;
        }
    }

    Lokale locale = Lokale.ENGELSK;

    public void setLocale(Lokale locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale.tekst;
    }

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

    public boolean isEngangsstonad() {
        return engangsstonad;
    }

    public void setEngangsstonad(boolean engangsstonad) {
        this.engangsstonad = engangsstonad;
    }

    public boolean isSvangerskapspenger() {
        return svangerskapspenger;
    }

    public void setSvangerskapspenger(boolean svangerskapspenger) {
        this.svangerskapspenger = svangerskapspenger;
    }
}
