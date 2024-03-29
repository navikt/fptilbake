package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;


import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;

public class BehandlingOpprettingDto {

    private BehandlingType behandlingType;
    private boolean kanOppretteBehandling;

    public BehandlingOpprettingDto(BehandlingType behandlingType, boolean kanOppretteBehandling) {
        this.behandlingType = behandlingType;
        this.kanOppretteBehandling = kanOppretteBehandling;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public boolean isKanOppretteBehandling() {
        return kanOppretteBehandling;
    }
}
