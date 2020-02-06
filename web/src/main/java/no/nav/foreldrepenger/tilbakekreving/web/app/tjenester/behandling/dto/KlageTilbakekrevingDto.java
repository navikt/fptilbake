package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class KlageTilbakekrevingDto {

    @JsonProperty("behandlingId")
    private Long behandlingId;

    @JsonProperty("tilbakekrevingVedtakDato")
    private LocalDate vedtakDato;

    @JsonProperty("tilbakekrevingBehandlingType")
    private String behandlingType;

    public KlageTilbakekrevingDto(Long behandlingId, LocalDate vedtakDato, String behandlingType) {
        this.behandlingId = behandlingId;
        this.vedtakDato = vedtakDato;
        this.behandlingType = behandlingType;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public LocalDate getVedtakDato() {
        return vedtakDato;
    }

    public String getBehandlingType() {
        return behandlingType;
    }

}
