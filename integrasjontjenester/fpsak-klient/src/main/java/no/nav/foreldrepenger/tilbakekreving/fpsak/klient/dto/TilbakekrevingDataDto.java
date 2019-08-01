package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

public class TilbakekrevingDataDto {

    private String saksnummer;
    private String fagsakYtelseType;
    private String videreBehandling;

    public TilbakekrevingDataDto() {}

    public TilbakekrevingDataDto(String saksnummer,
                                 String fagsakYtelseType,
                                 String videreBehandling) {
        this.saksnummer = saksnummer;
        this.fagsakYtelseType = fagsakYtelseType;
        this.videreBehandling = videreBehandling;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public String getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public String getVidereBehandling() {
        return videreBehandling;
    }

    public boolean harTilbakekrevingValg() {
        return (saksnummer != null && videreBehandling != null);
    }
}
