package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public class FeiltubetalingÅrsakerYtelseTypeDto {


    private FagsakYtelseType ytelseType;

    private List<HendelseTypeMedNavnDto> feilutbetalingÅrsaker;

    FeiltubetalingÅrsakerYtelseTypeDto() {
        //for Jackson
    }

    public FeiltubetalingÅrsakerYtelseTypeDto(FagsakYtelseType ytelseType, List<HendelseTypeMedNavnDto> feilutbetalingÅrsaker) {
        this.ytelseType = ytelseType;
        this.feilutbetalingÅrsaker = feilutbetalingÅrsaker;
    }

    public FagsakYtelseType getYtelseType() {
        return ytelseType;
    }

    public void setYtelseType(FagsakYtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

    public List<HendelseTypeMedNavnDto> getFeilutbetalingÅrsaker() {
        return feilutbetalingÅrsaker;
    }

    public void setFeilutbetalingÅrsaker(List<HendelseTypeMedNavnDto> feilutbetalingÅrsaker) {
        this.feilutbetalingÅrsaker = feilutbetalingÅrsaker;
    }
}
