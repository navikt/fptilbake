package no.nav.journalpostapi.dto;

import no.nav.journalpostapi.Kode;

/**
 * se https://confluence.adeo.no/display/BOA/Behandlingstema
 */
public enum BehandlingTema implements Kode {

    FEILUTBETALING("ab0006"),
    TILBAKEBETALING("ab0007");

    private String kode;

    BehandlingTema(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
