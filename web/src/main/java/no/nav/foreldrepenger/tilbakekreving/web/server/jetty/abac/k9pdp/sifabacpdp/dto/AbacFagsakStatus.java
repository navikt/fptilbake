package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto;

public enum AbacFagsakStatus {
    OPPRETTET("Opprettet"),
    UNDER_BEHANDLING("Under behandling");

    private String eksternKode;

    AbacFagsakStatus(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
