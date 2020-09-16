package no.nav.journalpostapi.dto.sak;

import no.nav.journalpostapi.Kode;

public enum FagsakSystem implements Kode {
    FORELDREPENGELØSNINGEN("FS36"),
    K9SAK("FS22");

    private String kode;

    FagsakSystem(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
