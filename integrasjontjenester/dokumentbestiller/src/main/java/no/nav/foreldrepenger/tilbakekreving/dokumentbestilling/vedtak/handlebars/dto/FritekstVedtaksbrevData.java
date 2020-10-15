package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodeverdiSomKodeSerialiserer;

public class FritekstVedtaksbrevData implements HandlebarsData {

    private Språkkode språkkode = Språkkode.nb;

    @JsonProperty("ytelsetype")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private FagsakYtelseType ytelsetype;

    @JsonProperty("hovedresultat")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private VedtakResultatType hovedresultat;

    @JsonProperty("fritekst")
    private String fritekst;

    @JsonProperty("finnesVerge")
    private boolean finnesVerge;

    @JsonProperty("annenMottakerNavn")
    private String annenMottakerNavn;

    @JsonProperty("klagefrist-uker")
    private Integer klagefristUker;

    @Override
    public Språkkode getSpråkkode() {
        return språkkode;
    }

    private FritekstVedtaksbrevData(){
        // bruk bilder
    }

    public FagsakYtelseType getYtelsetype() {
        return ytelsetype;
    }

    public VedtakResultatType getHovedresultat() {
        return hovedresultat;
    }

    public String getFritekst() {
        return fritekst;
    }

    public boolean isFinnesVerge() {
        return finnesVerge;
    }

    public String getAnnenMottakerNavn() {
        return annenMottakerNavn;
    }

    public Integer getKlagefristUker() {
        return klagefristUker;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private FritekstVedtaksbrevData kladd = new FritekstVedtaksbrevData();

        private Builder() {
        }

        public Builder medSpråkKode(Språkkode språkkode){
            this.kladd.språkkode = språkkode;
            return this;
        }

        public Builder medYtelsetype(FagsakYtelseType ytelseType){
            this.kladd.ytelsetype = ytelseType;
            return this;
        }

        public Builder medHovedResultat(VedtakResultatType vedtakResultatType){
            this.kladd.hovedresultat = vedtakResultatType;
            return this;
        }

        public Builder medFritekst(String fritekst){
            this.kladd.fritekst = fritekst;
            return this;
        }

        public Builder medFinnesVerge(boolean finnesVerge){
            this.kladd.finnesVerge = finnesVerge;
            return this;
        }

        public Builder medAnnenMottakerNavn(String annenMottakerNavn){
            this.kladd.annenMottakerNavn = annenMottakerNavn;
            return this;
        }

        public Builder medKlagefristUker(Integer klagefristUker){
            this.kladd.klagefristUker = klagefristUker;
            return this;
        }

        public FritekstVedtaksbrevData build(){
            Objects.requireNonNull(this.kladd.ytelsetype,"ytleseType er ikke satt");
            Objects.requireNonNull(this.kladd.hovedresultat,"hovedresultat er ikke satt");
            Objects.requireNonNull(this.kladd.fritekst,"fritekst kan ikke være null");
            Objects.requireNonNull(this.kladd.klagefristUker,"klagefristUker er ikke satt");

            if(this.kladd.finnesVerge){
                Objects.requireNonNull(this.kladd.annenMottakerNavn,"annenMottakerNavn kan ikke være null");
            }
            return kladd;
        }
    }
}
