package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;

public class HendelseUndertypeMedNavnDto {

    @NotNull
    @ValidKodeverk
    @JsonProperty("underÅrsakKode")
    private HendelseUnderType hendelseUndertype;

    private String underÅrsak;

    private String kodeverk;

    HendelseUndertypeMedNavnDto() {
        // For CDI
    }

    public HendelseUndertypeMedNavnDto(HendelseUnderType hendelseUnderType) {
        this.hendelseUndertype = hendelseUnderType;
        this.kodeverk = hendelseUnderType.getKodeverk();
        this.underÅrsak = hendelseUnderType.getNavn();
    }

    public HendelseUnderType getHendelseUndertype() {
        return hendelseUndertype;
    }
}
