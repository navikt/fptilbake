package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public class UuidDto implements AbacDto {

    public static final String NAME = "uuid";

    public static final String DESC = "behandlingUUID";

    /**
     * Behandling UUID (nytt alternativ til intern behandlingId. Bør brukes av eksterne systemer).
     */
    @Valid
    @NotNull
    private UUID behandlingUuid;

    public UuidDto(String behandlingUuid) {
        this.behandlingUuid = UUID.fromString(behandlingUuid);
    }

    public UuidDto(UUID behandlingUuid) {
        this.behandlingUuid = behandlingUuid;
    }

    @JsonProperty(NAME)
    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, behandlingUuid);
    }

}
