package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodeverdiSomKodeSerialiserer;

public class HbTotalresultat {

    @JsonProperty("hovedresultat")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private VedtakResultatType hovedresultat;
    @JsonProperty("totalt-tilbakekreves-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal totaltTilbakekrevesBeløp;
    @JsonProperty("totalt-tilbakekreves-beløp-med-renter")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal totaltTilbakekrevesBeløpMedRenter;
    @JsonProperty("totalt-tilbakekreves-beløp-med-renter-uten-skatt")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal totaltTilbakekrevesBeløpMedRenterUtenSkatt;
    @JsonProperty("totalt-rentebeløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal totaltRentebeløp;

    private HbTotalresultat() {
    }

    public VedtakResultatType getHovedresultat() {
        return hovedresultat;
    }

    public boolean harSkattetrekk() {
        return totaltTilbakekrevesBeløpMedRenterUtenSkatt.compareTo(totaltTilbakekrevesBeløpMedRenter) < 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private HbTotalresultat kladd = new HbTotalresultat();

        public HbTotalresultat.Builder medHovedresultat(VedtakResultatType hovedresultat) {
            kladd.hovedresultat = hovedresultat;
            return this;
        }

        public HbTotalresultat.Builder medTotaltTilbakekrevesBeløp(BigDecimal totaltTilbakekrevesBeløp) {
            kladd.totaltTilbakekrevesBeløp = totaltTilbakekrevesBeløp;
            return this;
        }

        public HbTotalresultat.Builder medTotaltTilbakekrevesBeløpMedRenter(BigDecimal totaltTilbakekrevesBeløpMedRenter) {
            kladd.totaltTilbakekrevesBeløpMedRenter = totaltTilbakekrevesBeløpMedRenter;
            return this;
        }

        public HbTotalresultat.Builder medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal totaltTilbakekrevesBeløpMedRenterUtenSkatt) {
            kladd.totaltTilbakekrevesBeløpMedRenterUtenSkatt = totaltTilbakekrevesBeløpMedRenterUtenSkatt;
            return this;
        }

        public HbTotalresultat.Builder medTotaltRentebeløp(BigDecimal totaltRentebeløp) {
            kladd.totaltRentebeløp = totaltRentebeløp;
            return this;
        }

        public HbTotalresultat build() {
            Objects.requireNonNull(kladd.hovedresultat, "hovedresultat er ikke satt");
            Objects.requireNonNull(kladd.totaltTilbakekrevesBeløp, "totaltTilbakekrevesBeløp er ikke satt");
            Objects.requireNonNull(kladd.totaltTilbakekrevesBeløpMedRenter, "totaltTilbakekrevesBeløpMedRenter er ikke satt");
            Objects.requireNonNull(kladd.totaltRentebeløp, "totaltRentebeløp er ikke satt");
            Objects.requireNonNull(kladd.totaltTilbakekrevesBeløpMedRenterUtenSkatt, "totaltTilbakekrevesBeløpMedRenterUtenSkatt er ikke satt");
            return kladd;
        }

    }
}
