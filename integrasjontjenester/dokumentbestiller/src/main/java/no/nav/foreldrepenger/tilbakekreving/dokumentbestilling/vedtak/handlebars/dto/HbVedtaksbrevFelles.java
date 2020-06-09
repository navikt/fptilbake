package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilLangtNorskFormatSerialiserer;

public class HbVedtaksbrevFelles implements HandlebarsData {

    @JsonProperty("søker")
    private HbPerson søker;
    @JsonProperty("sak")
    private HbSak sak;
    @JsonProperty("varsel")
    private HbVarsel varsel;
    @JsonProperty("totalresultat")
    private HbTotalresultat totalresultat;
    @JsonProperty("hjemmel")
    private HbHjemmel hjemmel;
    @JsonProperty("konfigurasjon")
    private HbKonfigurasjon konfigurasjon;
    @JsonProperty("fritekst-oppsummering")
    private String fritekstOppsummering;
    @JsonProperty("behandling")
    private HbBehandling behandling;
    @JsonProperty("finnesVerge")
    private boolean finnesVerge;
    @JsonProperty("annenMottakerNavn")
    private String annenMottakerNavn;
    private Språkkode språkkode = Språkkode.nb;

    private HbVedtaksbrevDatoer datoer;

    private HbVedtaksbrevFelles() {
        //bruk Builder
    }

    public static HbVedtaksbrevFelles.Builder builder() {
        return new HbVedtaksbrevFelles.Builder();
    }

    public FagsakYtelseType getYtelsetype() {
        return sak.getYtelsetype();
    }

    public String getFritekstOppsummering() {
        return fritekstOppsummering;
    }

    public void setFritekstOppsummering(String fritekstOppsummering) {
        this.fritekstOppsummering = fritekstOppsummering;
    }

    public HbBehandling getBehandling() {
        return behandling;
    }

    public boolean isFinnesVerge() {
        return finnesVerge;
    }

    public String getAnnenMottakerNavn() {
        return annenMottakerNavn;
    }

    @Override
    public Språkkode getSpråkkode() {
        return språkkode;
    }

    @JsonProperty("opphørsdato-død-søker")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate getOpphørsdatoDødSøker() {
        return datoer != null ? datoer.getOpphørsdatoDødSøker() : null;
    }

    @JsonProperty("opphørsdato-dødt-barn")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate getOpphørsdatoDødtBarn() {
        return datoer != null ? datoer.getOpphørsdatoDødtBarn() : null;
    }

    @JsonProperty("opphørsdato-ikke-gravid")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate getOpphørsdatoIkkeGravid() {
        return datoer != null ? datoer.getOpphørsdatoIkkeGravid() : null;
    }

    @JsonProperty("opphørsdato-ikke-omsorg")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate getOpphørsdatoIkkeOmsorg() {
        return datoer != null ? datoer.getOpphørsdatoIkkeOmsorg() : null;
    }

    public VedtakResultatType getHovedresultat() {
        return totalresultat.getHovedresultat();
    }

    public static class Builder {

        private HbVedtaksbrevFelles kladd = new HbVedtaksbrevFelles();

        private Builder() {
        }

        public HbVedtaksbrevFelles build() {
            java.util.Objects.requireNonNull(kladd.hjemmel, "hjemmel er ikke satt");
            java.util.Objects.requireNonNull(kladd.søker, "søker er ikke satt");
            java.util.Objects.requireNonNull(kladd.sak, "sak-informasjon er ikke satt");
            java.util.Objects.requireNonNull(kladd.konfigurasjon, "konfigurasjon er ikke satt");
            java.util.Objects.requireNonNull(kladd.totalresultat, "totalresultat er ikke satt");
            if (kladd.varsel == null) {
                java.util.Objects.requireNonNull(kladd.sak.harDatoForFagsakvedtak(), "dato for fagsakvedtak/revurdering er ikke satt");
            }
            if(kladd.finnesVerge){
                java.util.Objects.requireNonNull(kladd.annenMottakerNavn, "annenMottakerNavn kan ikke være null");
            }
            return kladd;
        }

        public Builder medVedtakResultat(HbTotalresultat vedtakResultat) {
            kladd.totalresultat = vedtakResultat;
            return this;
        }

        public Builder medSøker(HbPerson person) {
            kladd.søker = person;
            return this;
        }

        public Builder medSak(HbSak sak) {
            kladd.sak = sak;
            return this;
        }

        public Builder medVarsel(HbVarsel varsel) {
            kladd.varsel = varsel;
            return this;
        }

        public Builder medKonfigurasjon(HbKonfigurasjon konfigurasjon) {
            kladd.konfigurasjon = konfigurasjon;
            return this;
        }

        public Builder medFritekstOppsummering(String fritekstOppsummering) {
            kladd.fritekstOppsummering = fritekstOppsummering;
            return this;
        }

        public Builder medLovhjemmelVedtak(String lovhjemmelVedtak) {
            kladd.hjemmel = HbHjemmel.builder()
                .medLovhjemmelVedtak(lovhjemmelVedtak)
                .build();
            return this;
        }

        public Builder medDatoer(HbVedtaksbrevDatoer datoer) {
            kladd.datoer = datoer;
            return this;
        }

        public Builder medBehandling(HbBehandling behandling) {
            kladd.behandling = behandling;
            return this;
        }

        public Builder medSpråkkode(Språkkode språkkode) {
            kladd.språkkode = språkkode;
            return this;
        }

        public Builder medFinnesVerge(boolean finnesVerge) {
            kladd.finnesVerge = finnesVerge;
            return this;
        }

        public Builder medAnnenMottakerNavn(String annenMottakerNavn) {
            kladd.annenMottakerNavn = annenMottakerNavn;
            return this;
        }
    }

}
