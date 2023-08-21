package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class KorrigertHenvisningDto implements AbacDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @NotNull
    @Valid
    private UUID eksternBehandlingUuid;

    public KorrigertHenvisningDto() {
        // for CDI
    }

    public KorrigertHenvisningDto(Long behandlingId, UUID eksternBehandlingUuid) {
        this.behandlingId = behandlingId;
        this.eksternBehandlingUuid = eksternBehandlingUuid;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public UUID getEksternBehandlingUuid() {
        return eksternBehandlingUuid;
    }

    public void setEksternBehandlingUuid(UUID eksternBehandlingUuid) {
        this.eksternBehandlingUuid = eksternBehandlingUuid;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
    }
}
