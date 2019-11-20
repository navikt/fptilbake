package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;
import no.nav.vedtak.util.Objects;

public class HbResultat {

    @JsonProperty("tilbakekreves-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal tilbakekrevesBeløp;
    @JsonProperty("tilbakekreves-beløp-med-renter")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal tilbakekrevesBeløpMedRenter;
    @JsonProperty("renter-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal renterBeløp;

    private HbResultat() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HbResultat forTilbakekrevesBeløp(BigDecimal tilbakekrevesBeløp) {
        return HbResultat.builder()
            .medTilbakekrevesBeløp(tilbakekrevesBeløp)
            .build();
    }

    public static class Builder {

        private HbResultat kladd = new HbResultat();

        public HbResultat.Builder medTilbakekrevesBeløp(BigDecimal tilbakekrevesBeløp) {
            kladd.tilbakekrevesBeløp = tilbakekrevesBeløp;
            return this;
        }

        public HbResultat.Builder medRenterBeløp(BigDecimal renterBeløp) {
            kladd.renterBeløp = renterBeløp;
            return this;
        }

        public HbResultat build() {
            Objects.check(kladd.tilbakekrevesBeløp != null, "tilbakekrevesbeløp er ikke satt");
            kladd.tilbakekrevesBeløpMedRenter = kladd.renterBeløp != null
                ? kladd.tilbakekrevesBeløp.add(kladd.renterBeløp)
                : kladd.tilbakekrevesBeløp;
            return kladd;
        }
    }
}
