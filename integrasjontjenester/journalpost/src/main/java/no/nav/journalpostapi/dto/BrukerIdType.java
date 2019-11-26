package no.nav.journalpostapi.dto;

import no.nav.journalpostapi.Kode;

enum BrukerIdType implements Kode {
    NorskIdent("FNR"),
    Organisasjonsnummer("ORGNR"),
    AktørId("AKTOERID");

    private String kode;

    BrukerIdType(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
