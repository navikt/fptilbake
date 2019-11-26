package no.nav.journalpostapi.dto;

/**
 * se https://confluence.adeo.no/display/BOA/Behandlingstema
 */
public enum BehandlingTema {

    FEILUTBETALING("ab0006"),
    TILBAKEBETALING("ab0007");

    private String kode;

    BehandlingTema(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
