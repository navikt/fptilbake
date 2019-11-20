package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;

public class HbKravgrunnlag {

    @JsonProperty("riktig-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal riktigBeløp;
    @JsonProperty("utbetalt-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal utbetaltBeløp;
    @JsonProperty("feilutbetalt-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal feilutbetaltBeløp;

    private HbKravgrunnlag() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HbKravgrunnlag forFeilutbetaltBeløp(BigDecimal feilutbetaltBeløp) {
        HbKravgrunnlag kravgrunnlag = new HbKravgrunnlag();
        kravgrunnlag.feilutbetaltBeløp = feilutbetaltBeløp;
        return kravgrunnlag;
    }

    public boolean harRiktigOgUtbetaltBeløp() {
        return utbetaltBeløp != null && riktigBeløp != null;
    }

    public static class Builder {
        private HbKravgrunnlag kladd = new HbKravgrunnlag();

        public HbKravgrunnlag.Builder medRiktigBeløp(BigDecimal riktigBeløp) {
            kladd.riktigBeløp = riktigBeløp;
            return this;
        }

        public HbKravgrunnlag.Builder medUtbetaltBeløp(BigDecimal utbetaltBeløp) {
            kladd.utbetaltBeløp = utbetaltBeløp;
            return this;
        }

        public HbKravgrunnlag.Builder medFeilutbetaltBeløp(BigDecimal feilutbetaltBeløp) {
            kladd.feilutbetaltBeløp = feilutbetaltBeløp;
            return this;
        }

        public HbKravgrunnlag build() {
            Objects.requireNonNull(kladd.feilutbetaltBeløp, "mangler feilutbetalt beløp");
            return kladd;
        }

    }


}
