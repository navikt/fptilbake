package no.nav.journalpostapi.dto.dokument;

import no.nav.journalpostapi.Kode;

public enum Filtype implements Kode {

    PDF,
    PDFA,
    XML,
    RTF,
    DLF,
    JPEG,
    TIFF,
    AXML,
    DXML,
    JSON,
    PNG;

    @Override
    public String getKode() {
        return name();
    }
}
