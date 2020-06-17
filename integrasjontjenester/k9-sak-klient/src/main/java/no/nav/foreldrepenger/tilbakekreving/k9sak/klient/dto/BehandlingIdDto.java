package no.nav.foreldrepenger.tilbakekreving.k9sak.klient.dto;

import java.util.UUID;

public class BehandlingIdDto {

    private String behandlingId;

    public BehandlingIdDto(String behandlingId) {
        this.behandlingId = behandlingId;
    }

    public BehandlingIdDto(UUID behandlingUuid) {
        this.behandlingId = behandlingUuid.toString();
    }

    public String getBehandlingId() {
        return behandlingId;
    }
}
