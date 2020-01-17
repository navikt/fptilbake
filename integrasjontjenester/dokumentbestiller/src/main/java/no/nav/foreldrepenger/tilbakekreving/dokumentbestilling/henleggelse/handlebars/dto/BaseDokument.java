package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.handlebars.dto;

public class BaseDokument {

    private String fagsaktypeNavn;

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
}
