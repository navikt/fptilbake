package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.handlebars.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodeverdiSomKodeSerialiserer;

public class BaseDokument {

    @JsonProperty("ytelsetype")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private FagsakYtelseType ytelsetype;

    private String fagsaktypeNavn;
    private boolean isKorrigert;

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

    public boolean isKorrigert() {
        return isKorrigert;
    }

    public void setKorrigert(boolean korrigert) {
        isKorrigert = korrigert;
    }

    public boolean isYtelseUtenSkatt() {
        return erYtelseType(FagsakYtelseType.ENGANGSTØNAD);
    }

    public boolean isYtelseMedSkatt() {
        return erYtelseType(FagsakYtelseType.FORELDREPENGER) || erYtelseType(FagsakYtelseType.SVANGERSKAPSPENGER) || erYtelseType(FagsakYtelseType.FRISINN);
    }

    @JsonProperty("skal-vise-renteinformasjon")
    public boolean isSkalViseRenteinformasjon() {
        return erYtelseType(FagsakYtelseType.FORELDREPENGER) || erYtelseType(FagsakYtelseType.SVANGERSKAPSPENGER) || erYtelseType(FagsakYtelseType.ENGANGSTØNAD);
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

    private boolean erYtelseType(FagsakYtelseType ytelseType) {
        return ytelseType.equals(this.ytelsetype);
    }
}
