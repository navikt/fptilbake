package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;

public class HendelseTypeDto {

    @NotNull
    @ValidKodeverk
    @JsonProperty("årsakKode")
    private HendelseType hendelseType;

    @Size(max = 1)
    @Valid
    private List<HendelseUndertypeDto> underÅrsaker = new ArrayList<>();

    public HendelseType getHendelseType() {
        return hendelseType;
    }

    public void setHendelseType(HendelseType hendelseType) {
        this.hendelseType = hendelseType;
    }

    public HendelseUnderType getHendelseUndertype() {
        return underÅrsaker.isEmpty() ? null : underÅrsaker.get(0).getHendelseUndertype();
    }

    public void setHendelseUndertype(HendelseUnderType hendelseUndertype) {
        underÅrsaker.clear();
        underÅrsaker.add(new HendelseUndertypeDto(hendelseUndertype));
    }
}
