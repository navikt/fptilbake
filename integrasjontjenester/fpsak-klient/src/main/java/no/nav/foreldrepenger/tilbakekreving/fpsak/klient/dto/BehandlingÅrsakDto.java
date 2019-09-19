package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BehandlingÅrsakDto {

    @JsonProperty("behandlingArsakType")
    private BehandlingÅrsakType behandlingÅrsakType;

    public BehandlingÅrsakDto() {
        // trengs for deserialisering av JSON
    }

    public BehandlingÅrsakType getBehandlingÅrsakType() {
        return behandlingÅrsakType;
    }

    public void setBehandlingÅrsakType(BehandlingÅrsakType behandlingÅrsakType) {
        this.behandlingÅrsakType = behandlingÅrsakType;
    }
}
