package no.nav.journalpostapi.dto;

/**
 * se https://confluence.adeo.no/display/BOA/Tema
 */
public enum Tema {

    FORELDREPENGER_SVANGERSKAPSPENGER("FOR"),
    OMSORGSPENGER_PLEIEPENGER_OPPLÆRINGSPENGER("OMS"),
    BARNETRYGD("BAR"),
    KONTANTSTØTTE("KON"),
    SUPPLERENDE_STØNAD("SUP"),
    SYKEPENGER("SYK");

    private String kode;

    Tema(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
