package no.nav.foreldrepenger.tilbakekreving.k9sak.klient.simulering.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BehandlingReferanseDomene {
    @Valid
    @NotNull
    @JsonProperty(required = true, value = "behandlingReferanse")
    private BehandlingReferanse behandlingReferanse;

    @Valid
    @NotNull
    @JsonProperty(required = true, value = "domene")
    private Domene domene;

    @JsonCreator
    public BehandlingReferanseDomene(@JsonProperty(required = true, value = "behandlingReferanse") BehandlingReferanse behandlingReferanse, @JsonProperty(required = true, value = "domene") Domene domene) {
        this.behandlingReferanse = behandlingReferanse;
        this.domene = domene;
    }

    public BehandlingReferanse getBehandlingReferanse() {
        return behandlingReferanse;
    }

    public Domene getDomene() {
        return domene;
    }
}
