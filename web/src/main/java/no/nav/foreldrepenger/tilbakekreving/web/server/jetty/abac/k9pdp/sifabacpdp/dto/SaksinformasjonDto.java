package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SaksinformasjonDto(
    @Valid
    @Pattern(regexp = "^([A-ZÆØÅ][0-9]{6}$)|(VL)")
    String identAnsvarligSaksbehandler,

    AbacBehandlingStatus behandlingStatus,
    AbacFagsakStatus fagsakStatus,

    @Size(min = 0, max = 4)
    Set<@Valid AksjonspunktType> aksjonspunktTyper) {
}
