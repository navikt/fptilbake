package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaksnummerDto(

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Size(min = 5, max = 19)
    @Pattern(regexp = "^[a-zA-Z0-9]*$")
    String saksnummer) {
}
