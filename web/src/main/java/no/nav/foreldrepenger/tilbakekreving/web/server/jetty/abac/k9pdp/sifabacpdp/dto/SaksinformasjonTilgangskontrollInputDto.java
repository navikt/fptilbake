package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SaksinformasjonTilgangskontrollInputDto(

    @Valid
    SaksnummerDto saksnummer,

    @Valid
    @NotNull
    OperasjonDto operasjon,

    @Valid
    SaksinformasjonDto saksinformasjon) {

    @JsonIgnore
    @AssertFalse(message = "saksinformasjon er påkrevet for UPDATE på FAGSAK, men saksinformasjon er null")
    public boolean isSaksinformasjonMangler() {
        return saksinformasjon == null && operasjon.action() == BeskyttetRessursActionAttributt.UPDATE && operasjon.resource() == ResourceType.FAGSAK;
    }

    @JsonIgnore
    @AssertFalse(message = "saksnummer er påkrevet for FAGSAK, men er null")
    public boolean isSaksnummerMangler() {
        return saksnummer == null && operasjon.resource() == ResourceType.FAGSAK;
    }

}

