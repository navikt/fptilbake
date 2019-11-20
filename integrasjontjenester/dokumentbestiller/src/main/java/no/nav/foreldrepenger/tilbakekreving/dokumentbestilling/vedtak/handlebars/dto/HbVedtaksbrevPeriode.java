package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.util.Objects;

public class HbVedtaksbrevPeriode implements HandlebarsData {

    //TODO legg i fakta?
    @JsonProperty("riktig-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal riktigBeløp;
    @JsonProperty("utbetalt-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal utbetaltBeløp;

    //TODO samle beløpene i et resultat-objekt
    @JsonProperty("feilutbetalt-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal feilutbetaltBeløp;
    @JsonProperty("tilbakekreves-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal tilbakekrevesBeløp;
    @JsonProperty("tilbakekreves-beløp-med-renter")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal tilbakekrevesBeløpMedRenter;
    @JsonProperty("renter-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal renterBeløp;

    @JsonProperty("periode")
    private HbPeriode periode;
    @JsonProperty("fakta")
    private HbFakta fakta;
    @JsonProperty("vurderinger")
    private HbVurderinger vurderinger;

    private HbVedtaksbrevPeriode() {
        //bruk builder
    }

    public Periode getPeriode() {
        return periode.tilPeriode();
    }

    public HbFakta getFakta() {
        return fakta;
    }

    public HbVurderinger getVurderinger() {
        return vurderinger;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private HbVedtaksbrevPeriode kladd = new HbVedtaksbrevPeriode();

        private Builder() {
        }


        public Builder medPeriode(Periode periode) {
            kladd.periode = HbPeriode.of(periode);
            return this;
        }

        public Builder medFakta(HbFakta fakta) {
            kladd.fakta = fakta;
            return this;
        }

        public Builder medVurderinger(HbVurderinger vurderinger) {
            kladd.vurderinger = vurderinger;
            return this;
        }

        public Builder medFakta(HendelseType hendelseType, HendelseUnderType hendelseUnderType) {
            return medFakta(hendelseType, hendelseUnderType, null);
        }

        public Builder medFakta(HendelseType hendelseType, HendelseUnderType hendelseUnderType, String fritekstFakta) {
            kladd.fakta = HbFakta.builder()
                .medHendelsetype(hendelseType)
                .medHendelseUndertype(hendelseUnderType)
                .medFritekstFakta(fritekstFakta)
                .build();
            return this;
        }

        public Builder medRiktigBeløp(BigDecimal riktigBeløp) {
            kladd.riktigBeløp = riktigBeløp;
            return this;
        }

        public Builder medUtbetaltBeløp(BigDecimal utbetaltBeløp) {
            kladd.utbetaltBeløp = utbetaltBeløp;
            return this;
        }

        public Builder medFeilutbetaltBeløp(BigDecimal feilutbetaltBeløp) {
            kladd.feilutbetaltBeløp = feilutbetaltBeløp;
            return this;
        }

        public Builder medTilbakekrevesBeløp(BigDecimal tilbakekrevesBeløp) {
            kladd.tilbakekrevesBeløp = tilbakekrevesBeløp;
            return this;
        }

        public Builder medRenterBeløp(BigDecimal renterBeløp) {
            kladd.renterBeløp = renterBeløp;
            return this;
        }


        public HbVedtaksbrevPeriode build() {
            Objects.check(kladd.periode != null, "periode er ikke satt");
            Objects.check(kladd.fakta != null, "fakta er ikke satt");
            Objects.check(kladd.vurderinger != null, "vurderinger er ikke satt");

            Objects.check(kladd.tilbakekrevesBeløp != null, "tilbakekrevesbeløp er ikke satt");
            if (HendelseType.ØKONOMI_FEIL.equals(kladd.fakta.getHendelsetype()) || HendelseType.ES_FEIL_UTBETALING_TYPE.equals(kladd.fakta.getHendelsetype())) {
                Objects.check(kladd.riktigBeløp != null, "riktig beløp er ikke satt");
                Objects.check(kladd.utbetaltBeløp != null, "utbetalt beløp er ikke satt");
            }

            kladd.tilbakekrevesBeløpMedRenter = kladd.renterBeløp != null
                ? kladd.tilbakekrevesBeløp.add(kladd.renterBeløp)
                : kladd.tilbakekrevesBeløp;

            return kladd;
        }

    }
}
