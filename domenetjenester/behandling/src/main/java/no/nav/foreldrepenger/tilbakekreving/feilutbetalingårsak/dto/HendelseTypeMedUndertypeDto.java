package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public class HendelseTypeMedUndertypeDto {

    @NotNull
    @Valid
    private HendelseType hendelseType;

    @NotNull
    @Valid
    private HendelseUnderType hendelseUndertype;

    public HendelseTypeMedUndertypeDto() {
    }

    public HendelseTypeMedUndertypeDto(HendelseType hendelseType, HendelseUnderType hendelseUndertype) {
        this.hendelseType = hendelseType;
        this.hendelseUndertype = hendelseUndertype;
    }

    public HendelseType getHendelseType() {
        return hendelseType;
    }

    public HendelseUnderType getHendelseUndertype() {
        return hendelseUndertype;
    }
}
