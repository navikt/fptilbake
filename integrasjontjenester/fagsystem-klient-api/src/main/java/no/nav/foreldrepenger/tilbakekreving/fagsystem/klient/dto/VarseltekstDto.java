package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import no.nav.vedtak.util.InputValideringRegex;

public class VarseltekstDto {

    @NotNull
    @Size(max = 12000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String varseltekst;

    VarseltekstDto() {
        // Jackson
    }

    public VarseltekstDto(String varseltekst) {
        this.varseltekst = varseltekst;
    }

    public String getVarseltekst() {
        return varseltekst;
    }
}
