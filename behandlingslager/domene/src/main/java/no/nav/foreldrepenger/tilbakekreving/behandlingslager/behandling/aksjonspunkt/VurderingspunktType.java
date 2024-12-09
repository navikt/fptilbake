package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

public enum VurderingspunktType {
    INN("INN"),
    UT("UT");

    private final String dbKode;

    private VurderingspunktType(String dbKode) {
        this.dbKode = dbKode;
    }

    public String getDbKode() {
        return dbKode;
    }

    public static VurderingspunktType getType(String kode) {
        return switch (kode) {
            case "INN" -> INN;
            case "UT" -> UT;
            default -> throw new IllegalArgumentException("Ukjent kode: " + kode);
        };
    }
}
