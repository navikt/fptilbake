package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BehandlingRelLinkPayloadDto {

    private Long saksnummer;
    private Long behandlingId;

    public Long getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(Long saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }
}
