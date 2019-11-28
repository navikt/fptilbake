package no.nav.journalpostapi.dto;

import no.nav.journalpostapi.Kode;

public enum SenderMottakerIdType implements Kode {

    NorskIdent("FNR"),
    Organisasjonsnummer("ORGNR"),
    HPRNR("HPRNR"),
    UTL_ORG("UTL_ORG");

    private String kode;

    SenderMottakerIdType(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
