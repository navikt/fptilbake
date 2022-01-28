package no.nav.foreldrepenger.tilbakekreving.varselrespons;

public enum ResponsKanal {

    MANUELL("MANU"), //$NON-NLS-1$
    JOURNAL("JOUR"), //$NON-NLS-1$
    SELVBETJENING("SLVB"); //$NON-NLS-1$

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
            default -> throw new IllegalArgumentException("Ukjent kode: " + kode); //$NON-NLS-1$
        };
    }

}
