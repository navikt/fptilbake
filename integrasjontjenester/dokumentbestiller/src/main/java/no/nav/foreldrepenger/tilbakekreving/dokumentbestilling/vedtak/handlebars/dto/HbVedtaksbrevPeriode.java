package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Vurdering;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodelisteSomKodeSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilStrengMedNorskFormatSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.util.Objects;

public class HbVedtaksbrevPeriode implements HandlebarsData {

    @JsonProperty("fom")
    @JsonSerialize(using = LocalDateTilStrengMedNorskFormatSerialiserer.class)
    private LocalDate fom;
    @JsonProperty("tom")
    @JsonSerialize(using = LocalDateTilStrengMedNorskFormatSerialiserer.class)
    private LocalDate tom;
    @JsonProperty("hendelsetype")
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private HendelseType hendelsetype;
    @JsonProperty("hendelseundertype")
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private HendelseUnderType hendelseundertype;
    @JsonProperty("fritekst-fakta")
    private String fritekstFakta;

    @JsonProperty("vilkår-resultat")
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private VilkårResultat vilkårResultat;
    @JsonProperty("fritekst-vilkår")
    private String fritekstVilkår;
    @JsonProperty("aktsomhet-resultat")
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Vurdering aktsomhetResultat;

    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    @JsonProperty("foreldelsevurdering")
    private ForeldelseVurderingType foreldelsevurdering;
    @JsonProperty("foreldet-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal foreldetBeløp;

    @JsonProperty("riktig-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal riktigBeløp;
    @JsonProperty("utbetalt-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal utbetaltBeløp;
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
    @JsonProperty("beløp-i-behold")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal beløpIBehold;

    @JsonProperty("særlig-grunn-størrelse")
    private boolean særligGrunnStørrelse;
    @JsonProperty("særlig-grunn-annet")
    private boolean særligGrunnAnnet;
    @JsonProperty("særlig-grunn-nav-feil")
    private boolean særligGrunnNav;
    @JsonProperty("særlig-grunn-tid")
    private boolean særligGrunnTid;
    @JsonProperty("fritekst-særlige-grunner")
    private String fritekstSærligeGrunner;

    @JsonProperty("unntas-innkreving-pga-lavt-beløp")
    private boolean unntasInnkrevingPgaLavtBeløp;

    private HbVedtaksbrevPeriode() {
        //bruk builder
    }

    public Periode getPeriode() {
        return Periode.of(fom, tom);
    }

    public void setFritekstFakta(String fritekstFakta) {
        this.fritekstFakta = fritekstFakta;
    }

    public String getFritekstFakta() {
        return fritekstFakta;
    }

    public void setFritekstSærligeGrunner(String fritekstSærligeGrunner) {
        this.fritekstSærligeGrunner = fritekstSærligeGrunner;
    }

    public String getFritekstSærligeGrunner() {
        return fritekstSærligeGrunner;
    }

    public void setFritekstVilkår(String fritekstVilkår) {
        this.fritekstVilkår = fritekstVilkår;
    }

    public String getFritekstVilkår() {
        return fritekstVilkår;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private HbVedtaksbrevPeriode kladd = new HbVedtaksbrevPeriode();

        private Builder() {
        }


        public Builder medPeriode(Periode periode) {
            kladd.fom = periode.getFom();
            kladd.tom = periode.getTom();
            return this;
        }

        public Builder medHendelsetype(HendelseType hendelsetype) {
            kladd.hendelsetype = hendelsetype;
            return this;
        }

        public Builder medHendelseUndertype(HendelseUnderType hendelseUnderType) {
            kladd.hendelseundertype = hendelseUnderType;
            return this;
        }

        public Builder medVilkårResultat(VilkårResultat vilkårResultat) {
            kladd.vilkårResultat = vilkårResultat;
            return this;
        }

        public Builder medFritekstFakta(String fritekstFakta) {
            kladd.fritekstFakta = fritekstFakta;
            return this;
        }

        public Builder medFritekstVilkår(String fritekstVilkår) {
            kladd.fritekstVilkår = fritekstVilkår;
            return this;
        }

        public Builder medAktsomhetResultat(Vurdering vurdering) {
            kladd.aktsomhetResultat = vurdering;
            return this;
        }

        public Builder medForeldelsevurdering(ForeldelseVurderingType foreldelsevurdering) {
            kladd.foreldelsevurdering = foreldelsevurdering;
            return this;
        }

        public Builder medForeldetBeløp(BigDecimal foreldetBeløp) {
            kladd.foreldetBeløp = foreldetBeløp;
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

        public Builder medBeløpIBehold(BigDecimal beløpIBehold) {
            kladd.beløpIBehold = beløpIBehold;
            return this;
        }

        public Builder medFritekstSærligeGrunner(String fritekstSærligeGrunner) {
            kladd.fritekstSærligeGrunner = fritekstSærligeGrunner;
            return this;
        }

        public Builder medUnntasInnkrevingPgaLavtBeløp(boolean unntasInnkrevingPgaLavtBeløp) {
            kladd.unntasInnkrevingPgaLavtBeløp = unntasInnkrevingPgaLavtBeløp;
            return this;
        }

        public Builder medSærligeGrunner(Collection<SærligGrunn> særligeGrunner) {
            Set<SærligGrunn> grunner = new HashSet<>(særligeGrunner);
            kladd.særligGrunnAnnet = grunner.remove(SærligGrunn.ANNET);
            kladd.særligGrunnNav = grunner.remove(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL);
            kladd.særligGrunnStørrelse = grunner.remove(SærligGrunn.STØRRELSE_BELØP);
            kladd.særligGrunnTid = grunner.remove(SærligGrunn.TID_FRA_UTBETALING);
            grunner.remove(SærligGrunn.GRAD_AV_UAKTSOMHET);
            if (!grunner.isEmpty()) {
                throw new IllegalArgumentException("Ukjent særlig grunn: " + grunner);
            }
            return this;
        }

        public HbVedtaksbrevPeriode build() {
            Objects.check(kladd.fom != null, "fra og med dato er ikke satt");
            Objects.check(kladd.tom != null, "til og med dato er ikke satt");
            Objects.check(kladd.foreldelsevurdering != null, "foreldelsevurdering er ikke satt");
            if (ForeldelseVurderingType.IKKE_VURDERT.equals(kladd.foreldelsevurdering) ||
                ForeldelseVurderingType.IKKE_FORELDET.equals(kladd.foreldelsevurdering)) {
                Objects.check(kladd.vilkårResultat != null, "vilkårResultat er ikke satt");
            }
            Objects.check(kladd.hendelsetype != null, "hendelsetype er ikke satt");
            Objects.check(kladd.hendelseundertype != null, "hendelseundertype er ikke satt");
            Objects.check(kladd.tilbakekrevesBeløp != null, "tilbakekrevesbeløp er ikke satt");

            kladd.tilbakekrevesBeløpMedRenter = kladd.renterBeløp != null
                ? kladd.tilbakekrevesBeløp.add(kladd.renterBeløp)
                : kladd.tilbakekrevesBeløp;

            return kladd;
        }

    }
}
