package no.nav.journalpostapi.dto.dokument;

import no.nav.journalpostapi.Kode;

/**
 * https://confluence.adeo.no/display/BOA/Dokumentkategori
 */
public enum Dokumentkategori implements Kode {

    Brev("B"),
    Vedtaksbrev("VB"),
    Infobrev("IB"),
    Elektronisk_skjema("ES"),
    Tolkbart_skjema("TS"),
    Ikke_tolkbart_skjema("IS"),
    Konverterte_data_fra_system("KS"),
    Konvertert_fra_elektronisk_arkiv("KD"),
    SED("SED"),
    Pb_EØS("PUBL_BLANKETT_EOS"),
    Elektronisk_dialog("ELEKTRONISK_DIALOG"),
    Referat("REFERAT"),
    Forvaltningsnotat("FORVALTNINGSNOTAT"),
    Søknad("SOK"),
    Klage_eller_anke("KA");

    private String kode;

    Dokumentkategori(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
