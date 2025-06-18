package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BehandlingUuidOperasjonDto(
    @Valid @NotNull UUID behandlingUuid,
    @Valid @NotNull OperasjonDto operasjon
) {
}
