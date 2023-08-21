package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.vedtak.util.InputValideringRegex;

public class HentForhåndsvisningHenleggelseslbrevDto {

    @Valid
    @NotNull
    private BehandlingReferanse behandlingReferanse;

    @Size(max = 2000, message = "Fritekst er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekst;

    public BehandlingReferanse getBehandlingReferanse() {
        return behandlingReferanse;
    }

    @JsonIgnore
    public void setBehandlingReferanse(BehandlingReferanse behandlingReferanse) {
        this.behandlingReferanse = behandlingReferanse;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setFritekst(String fritekst) {
        this.fritekst = fritekst;
    }

    // TODO: K9-tilbake. fjern når endringen er merget og prodsatt også i fpsak-frontend
    @JsonSetter("behandlingId")
    @JsonProperty(value = "behandlingReferanse")
    public void setBehandlingId(BehandlingReferanse behandlingReferanse) {
        this.behandlingReferanse = behandlingReferanse;
    }

    @JsonSetter("behandlingUuid")
    @JsonProperty(value = "behandlingReferanse")
    public void setBehandlingUuid(BehandlingReferanse behandlingReferanse) {
        this.behandlingReferanse = behandlingReferanse;
    }

}
