package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodelisteSomKodeSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilStrengMedNorskFormatSerialiserer;
import no.nav.vedtak.util.Objects;

public class HbVedtaksbrevFelles implements HandlebarsData {
    @JsonProperty("ytelsetype")
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private FagsakYtelseType ytelsetype;
    @JsonProperty("er-fødsel")
    private boolean erFødsel;
    @JsonProperty("er-adopsjon")
    private boolean erAdopsjon;
    @JsonProperty("antall-barn")
    private Integer antallBarn;
    @JsonProperty("varslet-dato")
    @JsonSerialize(using = LocalDateTilStrengMedNorskFormatSerialiserer.class)
    private LocalDate varsletDato;
    @JsonProperty("varslet-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal varsletBeløp;
    @JsonProperty("hovedresultat")
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private VedtakResultatType hovedresultat;
    @JsonProperty("totalt-tilbakekreves-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal totaltTilbakekrevesBeløp;
    @JsonProperty("totalt-tilbakekreves-beløp-med-renter")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal totaltTilbakekrevesBeløpMedRenter;
    @JsonProperty("totalt-rentebeløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal totaltRentebeløp;
    @JsonProperty("fritekst-oppsummering")
    private String fritekstOppsummering;
    @JsonProperty("lovhjemmel-vedtak")
    private String lovhjemmelVedtak;
    @JsonProperty("lovhjemmel-flertall")
    private boolean lovhjemmelFlertall;
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

    private HbVedtaksbrevFelles() {
        //bruk Builder
    }

    public static HbVedtaksbrevFelles.Builder builder() {
        return new HbVedtaksbrevFelles.Builder();
    }

    public FagsakYtelseType getYtelsetype() {
        return ytelsetype;
    }

    public String getFritekstOppsummering() {
        return fritekstOppsummering;
    }

    public void setFritekstOppsummering(String fritekstOppsummering) {
        this.fritekstOppsummering = fritekstOppsummering;
    }

    public static class Builder {

        private HbVedtaksbrevFelles kladd = new HbVedtaksbrevFelles();

        private Builder() {
        }

        public HbVedtaksbrevFelles build() {
            Objects.check(kladd.erAdopsjon != kladd.erFødsel, "En og bare en av fødsel og adopsjon skal være satt");
            Objects.check(kladd.ytelsetype != null, "Ytelse type er ikke satt");
            Objects.check(kladd.antallBarn != null, "antallBarn er ikke satt");
            Objects.check(kladd.varsletDato != null, "varsletDato er ikke satt");
            Objects.check(kladd.varsletBeløp != null, "varsletBeløp er ikke satt");
            Objects.check(kladd.hovedresultat != null, "hovedresultat er ikke satt");
            Objects.check(kladd.lovhjemmelVedtak != null, "lovhjemmelVedtak er ikke satt");
            Objects.check(kladd.totaltTilbakekrevesBeløp != null, "totaltTilbakekrevesBeløp er ikke satt");
            Objects.check(kladd.totaltTilbakekrevesBeløpMedRenter != null, "totaltTilbakekrevesBeløpMedRenter er ikke satt");
            Objects.check(kladd.totaltRentebeløp != null, "totaltRentebeløp er ikke satt");
            Objects.check(kladd.fireRettsgebyr != null, "fireRettsgebyr er ikke satt");
            Objects.check(kladd.halvtGrunnbeløp != null, "halvtGrunnbeløp er ikke satt");
            Objects.check(kladd.klagefristUker != null, "klagefristUker er ikke satt");
            Objects.check(kladd.kontaktNavTelefon != null, "kontaktNavTelefon er ikke satt");
            Objects.check(kladd.kontaktNavInnkrevingTelefon != null, "kontaktNavInnkrevingTelefon er ikke satt");
            return kladd;
        }

        public Builder medYtelsetype(FagsakYtelseType ytelsetype) {
            kladd.ytelsetype = ytelsetype;
            return this;
        }

        public Builder medErFødsel(boolean erFødsel) {
            kladd.erFødsel = erFødsel;
            return this;
        }

        public Builder medErAdopsjon(boolean erAdopsjon) {
            kladd.erAdopsjon = erAdopsjon;
            return this;
        }

        public Builder medAntallBarn(int antallBarn) {
            kladd.antallBarn = antallBarn;
            return this;
        }

        public Builder medVarsletDato(LocalDate varsletDato) {
            kladd.varsletDato = varsletDato;
            return this;
        }

        public Builder medVarsletBeløp(BigDecimal varsletBeløp) {
            kladd.varsletBeløp = varsletBeløp;
            return this;
        }

        public Builder medHovedresultat(VedtakResultatType hovedresultat) {
            kladd.hovedresultat = hovedresultat;
            return this;
        }

        public Builder medTotaltTilbakekrevesBeløp(BigDecimal totaltTilbakekrevesBeløp) {
            kladd.totaltTilbakekrevesBeløp = totaltTilbakekrevesBeløp;
            return this;
        }

        public Builder medTotaltTilbakekrevesBeløpMedRenter(BigDecimal totaltTilbakekrevesBeløpMedRenter) {
            kladd.totaltTilbakekrevesBeløpMedRenter = totaltTilbakekrevesBeløpMedRenter;
            return this;
        }

        public Builder medTotaltRentebeløp(BigDecimal totaltRentebeløp) {
            kladd.totaltRentebeløp = totaltRentebeløp;
            return this;
        }

        public Builder medFritekstOppsummering(String fritekstOppsummering) {
            kladd.fritekstOppsummering = fritekstOppsummering;
            return this;
        }

        public Builder medLovhjemmelVedtak(String lovhjemmelVedtak) {
            return medLovhjemmelVedtak(lovhjemmelVedtak, " og ");
        }

        public Builder medLovhjemmelVedtak(String lovhjemmelVedtak, String skilletegnMellomHjemler) {
            kladd.lovhjemmelVedtak = lovhjemmelVedtak;
            kladd.lovhjemmelFlertall = lovhjemmelVedtak.contains(skilletegnMellomHjemler);
            return this;
        }

        public Builder medFireRettsgebyr(BigDecimal fireRettsgebyr) {
            kladd.fireRettsgebyr = fireRettsgebyr;
            return this;
        }

        public Builder medHalvtGrunnbeløp(BigDecimal halvtGrunnbeløp) {
            kladd.halvtGrunnbeløp = halvtGrunnbeløp;
            return this;
        }

        public Builder medKlagefristUker(int klagefristUker) {
            kladd.klagefristUker = klagefristUker;
            return this;
        }

        public Builder medKontaktNavTelefon(String kontaktNavTelefon) {
            kladd.kontaktNavTelefon = kontaktNavTelefon;
            return this;
        }

        public Builder medKontaktNavInnkrevingTelefon(String kontaktNavInnkrevingTelefon) {
            kladd.kontaktNavInnkrevingTelefon = kontaktNavInnkrevingTelefon;
            return this;
        }
    }
}
