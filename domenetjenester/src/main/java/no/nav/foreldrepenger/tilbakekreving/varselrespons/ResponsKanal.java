package no.nav.foreldrepenger.tilbakekreving.varselrespons;

public enum ResponsKanal {

    MANUELL("MANU"),
    JOURNAL("JOUR"),
    SELVBETJENING("SLVB");

    private final String dbKode;

    private ResponsKanal(String dbKode) {
        this.dbKode = dbKode;
    }

    public String getDbKode() {
        return dbKode;
    }

    public static ResponsKanal getResponsKanal(String kode) {
        return switch (kode) {
            case "MANU" -> MANUELL;
            case "JOUR" -> JOURNAL;
            case "SLVB" -> SELVBETJENING;
            default -> throw new IllegalArgumentException("Ukjent kode: " + kode);
        };
    }

}
