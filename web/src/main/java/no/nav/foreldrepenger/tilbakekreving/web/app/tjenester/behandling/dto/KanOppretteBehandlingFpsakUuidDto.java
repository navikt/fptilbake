package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public record KanOppretteBehandlingFpsakUuidDto(@NotNull @Valid UUID uuid) implements AbacDto {
    @Override
    public AbacDataAttributter abacAttributter() {
        // Tar ikke med ytelsesbehandlingUuid her siden metoden tar inn saksnummer som inneholder nok informasjon
        return AbacDataAttributter.opprett();
    }

}
