package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.util.InputValideringRegex;

public class VarseltekstDto {

    @NotNull
    @Size(max = 1500)
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
