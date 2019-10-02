package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;

public class HendelseTypeMedNavnDto {

    @NotNull
    @ValidKodeverk
    @JsonProperty("årsakKode")
    private HendelseType hendelseType;
    private String årsak;
    private String kodeverk;

    private List<HendelseUndertypeMedNavnDto> underÅrsaker = new ArrayList<>();

    public HendelseTypeMedNavnDto(HendelseType hendelseType, Collection<HendelseUnderType> undertyper) {
        this.hendelseType = hendelseType;
        this.årsak = hendelseType.getNavn();
        this.kodeverk = hendelseType.getKodeverk();
        this.underÅrsaker = undertyper.stream().map(HendelseUndertypeMedNavnDto::new).collect(Collectors.toList());
    }

    public HendelseType getHendelseType() {
        return hendelseType;
    }

    public List<HendelseUnderType> getHendelseUndertyper() {
        return underÅrsaker.stream().map(HendelseUndertypeMedNavnDto::getHendelseUndertype).collect(Collectors.toList());
    }
}
