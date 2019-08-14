package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public class FeiltubetalingÅrsakerYtelseTypeDto {

    private String ytelseType;

    private List<FeilutbetalingÅrsakDto> feilutbetalingÅrsaker;

    FeiltubetalingÅrsakerYtelseTypeDto() {
        //for Jackson
    }

    public FeiltubetalingÅrsakerYtelseTypeDto(FagsakYtelseType ytelseType, List<FeilutbetalingÅrsakDto> feilutbetalingÅrsaker) {
        this.ytelseType = ytelseType.getKode();
        this.feilutbetalingÅrsaker = feilutbetalingÅrsaker;
    }

    public String getYtelseType() {
        return ytelseType;
    }

    public void setYtelseType(String ytelseType) {
        this.ytelseType = ytelseType;
    }

    public List<FeilutbetalingÅrsakDto> getFeilutbetalingÅrsaker() {
        return feilutbetalingÅrsaker;
    }

    public void setFeilutbetalingÅrsaker(List<FeilutbetalingÅrsakDto> feilutbetalingÅrsaker) {
        this.feilutbetalingÅrsaker = feilutbetalingÅrsaker;
    }
}
