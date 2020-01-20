package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

public enum Lokale {
    BOKMÅL("nb_NO"),
    NYNORSK("nn_NO"),
    ENGELSK("en_GB");

    private final String tekst;

    Lokale(final String tekst) {
        this.tekst = tekst;
    }

    public String getTekst() {
        return tekst;
    }

    @Override
    public String toString() {
        return tekst;
    }
}
