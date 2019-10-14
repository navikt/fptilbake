package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public class HendelseTyperPrYtelseTypeDto {

    private FagsakYtelseType ytelseType;

    private List<HendelseTypeMedUndertyperDto> hendelseTyper;

    HendelseTyperPrYtelseTypeDto() {
        //for Jackson
    }

    public HendelseTyperPrYtelseTypeDto(FagsakYtelseType ytelseType, List<HendelseTypeMedUndertyperDto> feilutbetalingÅrsaker) {
        this.ytelseType = ytelseType;
        this.hendelseTyper = feilutbetalingÅrsaker;
    }

    public FagsakYtelseType getYtelseType() {
        return ytelseType;
    }

    public List<HendelseTypeMedUndertyperDto> getHendelseTyper() {
        return hendelseTyper;
    }

}
