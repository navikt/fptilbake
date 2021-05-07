package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class GjenopptaBehandlingDto implements AbacDto {

    @Valid
    @NotNull
    private BehandlingReferanse behandlingReferanse;

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingVersjon;

    public BehandlingReferanse getBehandlingReferanse() {
        return behandlingReferanse;
    }

    @JsonIgnore
    public void setBehandlingReferanse(BehandlingReferanse behandlingReferanse) {
        this.behandlingReferanse = behandlingReferanse;
    }

    // TODO: K9-tilbake. fjern når endringen er merget og prodsatt også i fpsak-frontend
    @JsonSetter("behandlingId")
    @JsonProperty(value = "behandlingReferanse")
    public void setBehandlingId(BehandlingReferanse behandlingReferanse) {
        this.behandlingReferanse = behandlingReferanse;
    }

    @JsonSetter("uuid")
    @JsonProperty(value = "behandlingReferanse")
    public void setBehandlingUuid(BehandlingReferanse behandlingReferanse) {
        this.behandlingReferanse = behandlingReferanse;
    }

    public Long getBehandlingVersjon() {
        return behandlingVersjon;
    }

    public void setBehandlingVersjon(Long behandlingVersjon) {
        this.behandlingVersjon = behandlingVersjon;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return BehandlingReferanseAbacAttributter.fraBehandlingReferanse(behandlingReferanse);
    }
}
