package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodeverdiSomKodeSerialiserer;

public class BaseDokument {

    @JsonProperty("ytelsetype")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private FagsakYtelseType ytelsetype;

    private String fagsaktypeNavn;

    public String getFagsaktypeNavn() {
        return fagsaktypeNavn;
    }

    public void setFagsaktypeNavn(String fagsaktypeNavn) {
        this.fagsaktypeNavn = fagsaktypeNavn;
    }

    public FagsakYtelseType getYtelsetype() {
        return ytelsetype;
    }

    public void setYtelsetype(FagsakYtelseType ytelsetype) {
        this.ytelsetype = ytelsetype;
    }

    public boolean isEngangsstønad() {
        return erYtelseType(FagsakYtelseType.ENGANGSTØNAD);
    }

    public boolean isForeldrepenger() {
        return erYtelseType(FagsakYtelseType.FORELDREPENGER);
    }

    public boolean isSvangerskapspenger() {
        return erYtelseType(FagsakYtelseType.SVANGERSKAPSPENGER);
    }

    protected boolean erYtelseType(FagsakYtelseType ytelseType) {
        return ytelseType.equals(this.ytelsetype);
    }
}
