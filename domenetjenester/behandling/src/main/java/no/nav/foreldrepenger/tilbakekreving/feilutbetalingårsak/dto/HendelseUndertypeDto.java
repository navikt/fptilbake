package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;

public class HendelseUndertypeDto {

    @NotNull
    @ValidKodeverk
    @JsonProperty("underÅrsakKode")
    private HendelseUnderType hendelseUndertype;

    HendelseUndertypeDto(){
    }

    public HendelseUndertypeDto(HendelseUnderType hendelseUndertype) {
        this.hendelseUndertype = hendelseUndertype;
    }

    public HendelseUnderType getHendelseUndertype() {
        return hendelseUndertype;
    }
}
