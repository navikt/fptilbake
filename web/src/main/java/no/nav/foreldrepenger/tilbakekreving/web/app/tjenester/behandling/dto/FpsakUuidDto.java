package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class FpsakUuidDto implements AbacDto {

    @NotNull
    @Valid
    private UUID uuid;

    public FpsakUuidDto(UUID uuid) {
        this.uuid = uuid;
    }

    public FpsakUuidDto(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        // Legger ikke til ytelsesbehandling siden vi allerede har saksnummer
        return AbacDataAttributter.opprett();
    }
}
