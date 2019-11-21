package no.nav.foreldrepenger.tilbakekreving.pdfgen;

public enum DocFormat {
    PDF, HTML, EMAIL;

    public String toString() {
        return name().toLowerCase();
    }
}
