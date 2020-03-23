package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto;

public enum Hendelse {
    TILBAKEKREVING_SPM("TILBAKEKREVING_SPM"),
    TILBAKEKREVING_FATTET_VEDTAK("TILBAKEKREVING_FATTET_VEDTAK"),
    TILBAKEKREVING_HENLAGT("TILBAKEKREVING_HENLAGT");

    private String kode;

    Hendelse(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
