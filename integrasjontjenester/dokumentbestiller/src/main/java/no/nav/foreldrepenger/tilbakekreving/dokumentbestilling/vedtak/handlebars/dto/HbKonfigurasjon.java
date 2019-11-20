package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;
import no.nav.vedtak.util.Objects;

public class HbKonfigurasjon {

    @JsonProperty("fire-rettsgebyr")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal fireRettsgebyr = BigDecimal.valueOf(4600);  //FIXME fjerne hardkoding
    @JsonProperty("halvt-grunnbeløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal halvtGrunnbeløp = BigDecimal.valueOf(49929);  //FIXME fjerne hardkoding
    @JsonProperty("klagefrist-uker")
    private Integer klagefristUker;
    @JsonProperty("kontakt-nav-telefon")
    private String kontaktNavTelefon = "55 55 33 33"; //TODO fjerne hardkoding
    @JsonProperty("kontakt-nav-innkreving-telefon")
    private String kontaktNavInnkrevingTelefon = "21 05 11 00";  //TODO fjerne hardkoding
    @JsonProperty("bruk-midlertidig-tekst")
    private boolean brukMidlertidigTekst = true;

    public HbKonfigurasjon() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean brukMidlertidigTekst() {
        return brukMidlertidigTekst;
    }

    public static class Builder {
        private HbKonfigurasjon kladd = new HbKonfigurasjon();

        public HbKonfigurasjon.Builder medFireRettsgebyr(BigDecimal fireRettsgebyr) {
            kladd.fireRettsgebyr = fireRettsgebyr;
            return this;
        }

        public HbKonfigurasjon.Builder medHalvtGrunnbeløp(BigDecimal halvtGrunnbeløp) {
            kladd.halvtGrunnbeløp = halvtGrunnbeløp;
            return this;
        }

        public HbKonfigurasjon.Builder medKlagefristUker(int klagefristUker) {
            kladd.klagefristUker = klagefristUker;
            return this;
        }

        public HbKonfigurasjon.Builder medKontaktNavTelefon(String kontaktNavTelefon) {
            kladd.kontaktNavTelefon = kontaktNavTelefon;
            return this;
        }

        public HbKonfigurasjon.Builder medKontaktNavInnkrevingTelefon(String kontaktNavInnkrevingTelefon) {
            kladd.kontaktNavInnkrevingTelefon = kontaktNavInnkrevingTelefon;
            return this;
        }

        public HbKonfigurasjon.Builder skruAvMidlertidigTekst() {
            kladd.brukMidlertidigTekst = false;
            return this;
        }

        public HbKonfigurasjon build() {
            Objects.check(kladd.fireRettsgebyr != null, "fireRettsgebyr er ikke satt");
            Objects.check(kladd.halvtGrunnbeløp != null, "halvtGrunnbeløp er ikke satt");
            Objects.check(kladd.klagefristUker != null, "klagefristUker er ikke satt");
            Objects.check(kladd.kontaktNavTelefon != null, "kontaktNavTelefon er ikke satt");
            Objects.check(kladd.kontaktNavInnkrevingTelefon != null, "kontaktNavInnkrevingTelefon er ikke satt");
            return kladd;
        }
    }
}
