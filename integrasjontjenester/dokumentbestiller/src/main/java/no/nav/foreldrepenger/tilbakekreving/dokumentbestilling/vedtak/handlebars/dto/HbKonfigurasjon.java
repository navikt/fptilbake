package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.felles.Rettsgebyr;

public class HbKonfigurasjon {

    @JsonProperty("fire-rettsgebyr")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal fireRettsgebyr = BigDecimal.valueOf(Rettsgebyr.GEBYR * 4);
    @JsonProperty("halvt-grunnbeløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal halvtGrunnbeløp = BigDecimal.valueOf(49929);  //FIXME fjerne hardkoding
    @JsonProperty("klagefrist-uker")
    private Integer klagefristUker;

    public HbKonfigurasjon() {
    }

    public static Builder builder() {
        return new Builder();
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

        public HbKonfigurasjon build() {
            Objects.requireNonNull(kladd.fireRettsgebyr, "fireRettsgebyr er ikke satt");
            Objects.requireNonNull(kladd.halvtGrunnbeløp, "halvtGrunnbeløp er ikke satt");
            Objects.requireNonNull(kladd.klagefristUker, "klagefristUker er ikke satt");
            return kladd;
        }
    }
}
