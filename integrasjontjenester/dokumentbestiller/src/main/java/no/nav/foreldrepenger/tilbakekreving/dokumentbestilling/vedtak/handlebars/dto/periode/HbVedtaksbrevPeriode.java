package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.dto.periode.HbPeriode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class HbVedtaksbrevPeriode {

    @JsonProperty("periode")
    private HbPeriode periode;
    @JsonProperty("delperioder")
    private List<HbVedtaksbrevPeriode> delperioder;
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

        public Builder medDelperioder(List<HbVedtaksbrevPeriode> delperioder) {
            kladd.delperioder = delperioder;
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
            Objects.requireNonNull(kladd.periode, "periode er ikke satt");
            Objects.requireNonNull(kladd.kravgrunnlag, "kravgrunnlag er ikke satt");
            Objects.requireNonNull(kladd.fakta, "fakta er ikke satt");
            Objects.requireNonNull(kladd.vurderinger, "vurderinger er ikke satt");
            Objects.requireNonNull(kladd.resultat, "resultat er ikke satt");

            if ((HendelseType.ØKONOMI_FEIL.equals(kladd.fakta.getHendelsetype()) || HendelseType.ES_FEIL_UTBETALING_TYPE.equals(kladd.fakta.getHendelsetype()))
                    && !kladd.kravgrunnlag.harRiktigOgUtbetaltBeløp()) {
                throw new IllegalArgumentException("har ikke satt riktig beløp og/eller utbetalt beløp");
            }

            return kladd;
        }
    }
}
