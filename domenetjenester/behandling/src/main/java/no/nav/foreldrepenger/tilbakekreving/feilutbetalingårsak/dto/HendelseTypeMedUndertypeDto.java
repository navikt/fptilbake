package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;

public class HendelseTypeMedUndertypeDto {

    @NotNull
    @ValidKodeverk
    private HendelseType hendelseType;

    @NotNull
    @ValidKodeverk
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
