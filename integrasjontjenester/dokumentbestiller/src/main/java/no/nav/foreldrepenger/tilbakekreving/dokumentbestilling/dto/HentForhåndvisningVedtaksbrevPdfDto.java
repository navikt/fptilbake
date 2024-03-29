package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.vedtak.util.InputValideringRegex;

public class HentForhåndvisningVedtaksbrevPdfDto {

    @Valid
    @NotNull
    private BehandlingReferanse behandlingReferanse;

    @Size(max = 10000, message = "Oppsummeringstekst er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String oppsummeringstekst;

    @Size(max = 50, message = "For mange perioder")
    @NotNull
    @Valid
    private List<PeriodeMedTekstDto> perioderMedTekst;

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
    public void setUuid(BehandlingReferanse behandlingReferanse) {
        this.behandlingReferanse = behandlingReferanse;
    }

    @JsonSetter("behandlingUuid")
    public void setBehandlingUuid(BehandlingReferanse behandlingReferanse) {
        this.behandlingReferanse = behandlingReferanse;
    }

    public String getOppsummeringstekst() {
        return oppsummeringstekst;
    }

    public void setOppsummeringstekst(String oppsummeringstekst) {
        this.oppsummeringstekst = oppsummeringstekst;
    }

    public List<PeriodeMedTekstDto> getPerioderMedTekst() {
        return perioderMedTekst;
    }

    public void setPerioderMedTekst(List<PeriodeMedTekstDto> perioderMedTekst) {
        this.perioderMedTekst = perioderMedTekst;
    }

}
