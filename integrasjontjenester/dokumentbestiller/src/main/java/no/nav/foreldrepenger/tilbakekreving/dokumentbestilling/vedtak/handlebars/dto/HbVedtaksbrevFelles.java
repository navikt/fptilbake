package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.vedtak.util.Objects;

public class HbVedtaksbrevFelles implements HandlebarsData {

    @JsonProperty("fritekst-oppsummering")
    private String fritekstOppsummering;
    @JsonProperty("lovhjemmel-vedtak")
    private String lovhjemmelVedtak;
    @JsonProperty("lovhjemmel-flertall")
    private boolean lovhjemmelFlertall;

    @JsonProperty("totalresultat")
    private HbTotalresultat totalresultat;
    @JsonProperty("sak")
    private HbSak sak;
    @JsonProperty("varsel")
    private HbVarsel varsel;
    @JsonProperty("konfigurasjon")
    private HbKonfigurasjon konfigurasjon;

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

    public VedtakResultatType getHovedresultat() {
        return totalresultat.getHovedresultat();
    }

    public static class Builder {

        private HbVedtaksbrevFelles kladd = new HbVedtaksbrevFelles();

        private Builder() {
        }

        public HbVedtaksbrevFelles build() {
            Objects.check(kladd.lovhjemmelVedtak != null, "lovhjemmelVedtak er ikke satt");
            Objects.check(kladd.sak != null, "sak-informasjon er ikke satt");
            Objects.check(kladd.konfigurasjon != null, "konfigurasjon er ikke satt");
            Objects.check(kladd.totalresultat != null, "totalresultat er ikke satt");
            if (kladd.konfigurasjon.brukMidlertidigTekst()) {
                Objects.check(kladd.totalresultat.harBeløpMedRenterUtenSkatt(), "totaltTilbakekrevesBeløpMedRenterUtenSkatt er ikke satt");
            }
            if (kladd.varsel == null) {
                Objects.check(kladd.sak.harDatoForFagsakvedtak(), "dato for fagsakvedtak/revurdering er ikke satt");
            }
            return kladd;
        }

        public Builder medVedtakResultat(HbTotalresultat vedtakResultat) {
            kladd.totalresultat = vedtakResultat;
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
            return medLovhjemmelVedtak(lovhjemmelVedtak, " og ");
        }

        public Builder medLovhjemmelVedtak(String lovhjemmelVedtak, String skilletegnMellomHjemler) {
            kladd.lovhjemmelVedtak = lovhjemmelVedtak;
            kladd.lovhjemmelFlertall = lovhjemmelVedtak.contains(skilletegnMellomHjemler);
            return this;
        }

    }
}
