package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class BestillBrevDto implements AbacDto {

    @NotNull
    @Valid
    private BehandlingReferanse behandlingReferanse;

    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String brevmalkode;

    @NotNull
    @Size(min = 1, max = 3000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekst;

    public BehandlingReferanse getBehandlingReferanse() {
        return behandlingReferanse;
    }

    public void setBehandlingReferanse(BehandlingReferanse behandlingReferanse) {
        this.behandlingReferanse = behandlingReferanse;
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

    public String getBrevmalkode() {
        return brevmalkode;
    }

    // TODO: K9-tilbake. fjern når endringen er merget og prodsatt også i fpsak-frontend
    @JsonSetter("brevmalkode")
    @JsonProperty(value = "brevmalkode")
    public void setBrevmalkode(String brevmalkode) {
        this.brevmalkode = brevmalkode;
    }

    // Harmonisering mellom fpsak/k9-sak og fptilbake/k9-tilbake. I frontend så brukes dokumentMal.
    @JsonSetter("dokumentMal")
    @JsonProperty(value = "brevmalkode")
    public void setDokumentMal(String brevmalkode) {
        this.brevmalkode = brevmalkode;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setFritekst(String fritekst) {
        this.fritekst = fritekst;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return behandlingReferanse.abacAttributter();
    }
}
