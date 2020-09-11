package no.nav.journalpostapi.dto.sak;

public enum FagsakSystem {
    FORELDREPENGELØSNINGEN("FS36"),
    K9SAK("K9");

    private String kode;

    FagsakSystem(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
