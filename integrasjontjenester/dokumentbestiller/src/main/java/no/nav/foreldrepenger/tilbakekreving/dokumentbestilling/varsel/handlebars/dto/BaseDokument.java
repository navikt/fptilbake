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

    public boolean isForeldrepenger() {
        return sjekkFagsakYtelseType(FagsakYtelseType.FORELDREPENGER);
    }

    public boolean isEngangsstønad() {
        return sjekkFagsakYtelseType(FagsakYtelseType.ENGANGSTØNAD);
    }

    public boolean isSvangerskapspenger() {
        return sjekkFagsakYtelseType(FagsakYtelseType.SVANGERSKAPSPENGER);
    }

    public boolean isKorrigert() {
        return isKorrigert;
    }

    public void setKorrigert(boolean korrigert) {
        isKorrigert = korrigert;
    }

    public boolean isFrisinn() {
        return sjekkFagsakYtelseType(FagsakYtelseType.FRISINN);
    }

    public boolean isYtelseUtenSkatt() {
        return isEngangsstønad();
    }

    public boolean isYtelseMedSkatt() {
        return isForeldrepenger() || isSvangerskapspenger() || isFrisinn();
    }

    private boolean sjekkFagsakYtelseType(FagsakYtelseType ytelseType) {
        return ytelseType.equals(this.ytelsetype);
    }
}
