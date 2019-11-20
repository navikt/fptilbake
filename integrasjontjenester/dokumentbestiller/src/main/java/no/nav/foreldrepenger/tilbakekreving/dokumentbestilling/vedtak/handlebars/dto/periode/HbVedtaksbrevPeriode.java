package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.util.Objects;

public class HbVedtaksbrevPeriode implements HandlebarsData {

    @JsonProperty("periode")
    private HbPeriode periode;
    @JsonProperty("kravgrunnlag")
    private HbKravgrunnlag kravgrunnlag;
    @JsonProperty("fakta")
    private HbFakta fakta;
    @JsonProperty("vurderinger")
    private HbVurderinger vurderinger;
    @JsonProperty("resultat")
    private HbResultat resultat;

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

        public Builder medKravgrunnlag(HbKravgrunnlag kravgrunnlag) {
            kladd.kravgrunnlag = kravgrunnlag;
            return this;
        }

        public Builder medVurderinger(HbVurderinger vurderinger) {
            kladd.vurderinger = vurderinger;
            return this;
        }

        public Builder medResultat(HbResultat resultat) {
            kladd.resultat = resultat;
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

        public HbVedtaksbrevPeriode build() {
            Objects.check(kladd.periode != null, "periode er ikke satt");
            Objects.check(kladd.kravgrunnlag != null, "kravgrunnlag er ikke satt");
            Objects.check(kladd.fakta != null, "fakta er ikke satt");
            Objects.check(kladd.vurderinger != null, "vurderinger er ikke satt");
            Objects.check(kladd.resultat != null, "resultat er ikke satt");

            if (HendelseType.ØKONOMI_FEIL.equals(kladd.fakta.getHendelsetype()) || HendelseType.ES_FEIL_UTBETALING_TYPE.equals(kladd.fakta.getHendelsetype())) {
                Objects.check(kladd.kravgrunnlag.harRiktigOgUtbetaltBeløp(), "har ikke satt riktig beløp og/eller utbetalt beløp");
            }

            return kladd;
        }

    }
}
