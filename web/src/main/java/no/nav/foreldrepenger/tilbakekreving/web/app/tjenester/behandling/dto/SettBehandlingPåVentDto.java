package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class SettBehandlingPåVentDto implements AbacDto {
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @Valid
    private UUID behandlingUuid;

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingVersjon;

    private LocalDate frist;

    @Valid
    private Venteårsak ventearsak;

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public void setBehandlingUuid(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }

    public Long getBehandlingVersjon() {
        return behandlingVersjon;
    }

    public void setBehandlingVersjon(Long behandlingVersjon) {
        this.behandlingVersjon = behandlingVersjon;
    }

    public LocalDate getFrist() {
        return frist;
    }

    public void setFrist(LocalDate frist) {
        this.frist = frist;
    }

    public Venteårsak getVentearsak() {
        return ventearsak;
    }

    public void setVentearsak(Venteårsak ventearsak) {
        this.ventearsak = ventearsak;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        var attributter = AbacDataAttributter.opprett();
        if (behandlingId != null) {
            attributter.leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, behandlingId);
        }
        if (behandlingUuid != null) {
            attributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, behandlingUuid);
        }
        return attributter;
    }

}
