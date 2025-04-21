package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

public enum K9AbacResultat {
    GODKJENT,
    AVSLÅTT_KODE_7,
    AVSLÅTT_KODE_6,
    AVSLÅTT_EGEN_ANSATT,
    AVSLÅTT_ANNEN_ÅRSAK;

    public boolean fikkTilgang() {
        return this == GODKJENT;
    }
}
