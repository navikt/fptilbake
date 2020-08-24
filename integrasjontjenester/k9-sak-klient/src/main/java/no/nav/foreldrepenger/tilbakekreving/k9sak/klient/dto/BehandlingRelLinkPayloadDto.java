package no.nav.foreldrepenger.tilbakekreving.k9sak.klient.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BehandlingRelLinkPayloadDto {

    private UUID behandlingUuid;

    public BehandlingRelLinkPayloadDto() {
    }

    public BehandlingRelLinkPayloadDto(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }

    public BehandlingRelLinkPayloadDto(String behandlingUuid) {
        this.behandlingUuid = UUID.fromString(behandlingUuid);
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public void setBehandlingUuid(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }
}
