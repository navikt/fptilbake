package no.nav.journalpostapi.dto.sak;

import no.nav.journalpostapi.Kode;

public enum Sakstype implements Kode {
    FAGSAK,
    GENERELL_SAK,
    ARKIVSAK;

    @Override
    public String getKode() {
        return name();
    }
}

