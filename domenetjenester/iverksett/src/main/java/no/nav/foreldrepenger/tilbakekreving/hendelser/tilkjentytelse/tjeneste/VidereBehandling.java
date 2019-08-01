package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

public enum VidereBehandling {

    TILBAKEKREV_I_INFOTRYGD ("TILBAKEKR_INFOTRYGD"),
    IGNORER_TILBAKEKREVING ("TILBAKEKR_IGNORER"),
    INNTREKK ("TILBAKEKR_INNTREKK");

    String kode;

    VidereBehandling(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

}
