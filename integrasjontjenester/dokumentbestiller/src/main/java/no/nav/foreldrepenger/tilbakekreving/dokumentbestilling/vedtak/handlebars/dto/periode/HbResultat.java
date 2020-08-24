package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;

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
    @JsonProperty("foreldet-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal foreldetBeløp;
    @JsonProperty("tilbakekreves-beløp-uten-skatt-med-renter")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal tilbakekrevesBeløpUtenSkattMedRenter;

    private HbResultat() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private HbResultat kladd = new HbResultat();
        private BigDecimal nettoBeløp;

        public HbResultat.Builder medTilbakekrevesBeløp(BigDecimal tilbakekrevesBeløp) {
            kladd.tilbakekrevesBeløp = tilbakekrevesBeløp;
            return this;
        }

        public HbResultat.Builder medTilbakekrevesBeløpUtenSkatt(BigDecimal nettoBeløp) {
            this.nettoBeløp = nettoBeløp;
            return this;
        }

        public HbResultat.Builder medRenterBeløp(BigDecimal renterBeløp) {
            kladd.renterBeløp = renterBeløp;
            return this;
        }

        public HbResultat.Builder medForeldetBeløp(BigDecimal foreldetBeløp) {
            kladd.foreldetBeløp = foreldetBeløp;
            return this;
        }

        public HbResultat build() {
            Objects.requireNonNull(kladd.tilbakekrevesBeløp, "tilbakekrevesbeløp er ikke satt");
            Objects.requireNonNull(kladd.renterBeløp, "renter er ikke satt");
            Objects.requireNonNull(nettoBeløp, "nettobeløp er ikke satt");
            kladd.tilbakekrevesBeløpMedRenter = kladd.tilbakekrevesBeløp.add(kladd.renterBeløp);
            kladd.tilbakekrevesBeløpUtenSkattMedRenter = nettoBeløp;
            return kladd;
        }
    }
}
