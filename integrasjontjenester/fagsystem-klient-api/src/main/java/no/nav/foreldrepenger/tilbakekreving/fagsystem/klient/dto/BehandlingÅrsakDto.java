package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;

public class BehandlingÅrsakDto {

    private BehandlingÅrsakType behandlingÅrsakType;

    public BehandlingÅrsakType getBehandlingÅrsakType() {
        return behandlingÅrsakType;
    }

    public void setBehandlingÅrsakType(BehandlingÅrsakType behandlingÅrsakType) {
        this.behandlingÅrsakType = behandlingÅrsakType;
    }
}
