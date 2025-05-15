package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto;

import jakarta.validation.constraints.NotNull;

public record OperasjonDto(
    @NotNull ResourceType resource,
    @NotNull BeskyttetRessursActionAttributt action) {
}


