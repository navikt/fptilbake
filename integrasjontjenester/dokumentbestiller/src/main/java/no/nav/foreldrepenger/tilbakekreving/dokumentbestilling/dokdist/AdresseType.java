package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;

import no.nav.journalpostapi.Kode;

public enum AdresseType implements Kode {
    NORSK("norskPostadresse"),
    UTENLANDSK("utenlandskPostadresse");

    private String kode;

    AdresseType(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
