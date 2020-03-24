package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto;

public enum Hendelse {
    TILBAKEKREVING_SPM("TILBAKEKREVING_SPM", "Utsendt tilbakekrevingsvarsel"),
    TILBAKEKREVING_FATTET_VEDTAK("TILBAKEKREVING_FATTET_VEDTAK", "Fattet vedtak"),
    TILBAKEKREVING_HENLAGT("TILBAKEKREVING_HENLAGT", "Behandling henlagt");

    private String kode;
    private String beskrivelse;

    Hendelse(String kode, String beskrivelse) {
        this.kode = kode;
        this.beskrivelse = beskrivelse;
    }

    public String getKode() {
        return kode;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }
}
