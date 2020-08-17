package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.EksternBehandlingÅrsakType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EksternBehandlingÅrsakDto {

    @JsonProperty("behandlingArsakType")
    private EksternBehandlingÅrsakType behandlingÅrsakType;

    public EksternBehandlingÅrsakDto() {
        // trengs for deserialisering av JSON
    }

    public EksternBehandlingÅrsakType getBehandlingÅrsakType() {
        return behandlingÅrsakType;
    }

    public void setBehandlingÅrsakType(EksternBehandlingÅrsakType behandlingÅrsakType) {
        this.behandlingÅrsakType = behandlingÅrsakType;
    }
}
