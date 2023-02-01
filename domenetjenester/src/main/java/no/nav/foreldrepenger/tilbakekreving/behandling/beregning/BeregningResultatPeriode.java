package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeResultat;

public class BeregningResultatPeriode {

    private Periode periode;
    private boolean erForeldet;
    private BigDecimal tilbakekrevingBeløp;
    private BigDecimal tilbakekrevingBeløpEtterSkatt;
    private BigDecimal tilbakekrevingBeløpUtenRenter;
    private BigDecimal renterProsent;
    private BigDecimal renteBeløp;
    private BigDecimal skattBeløp;
    private BigDecimal feilutbetaltBeløp;
    private BigDecimal utbetaltYtelseBeløp; //rått beløp, ikke justert for evt. trekk
    private BigDecimal riktigYtelseBeløp; //rått beløp, ikke justert for evt. trekk

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        BeregningResultatPeriode kladd = new BeregningResultatPeriode();
        Boolean erForeldet;

        private Builder() {
        }

        public Builder medPeriode(Periode periode) {
            kladd.periode = periode;
            return this;
        }


        public Builder medErForeldet(boolean erForeldet) {
            kladd.erForeldet = erForeldet;
            this.erForeldet = erForeldet;
            return this;
        }

        public Builder medTilbakekrevingBeløp(BigDecimal tilbakekrevingBeløp) {
            kladd.tilbakekrevingBeløp = tilbakekrevingBeløp;
            return this;
        }

        public Builder medTilbakekrevingBeløpEtterSkatt(BigDecimal tilbakekrevingBeløpEtterSkatt) {
            kladd.tilbakekrevingBeløpEtterSkatt = tilbakekrevingBeløpEtterSkatt;
            return this;
        }

        public Builder medTilbakekrevingBeløpUtenRenter(BigDecimal tilbakekrevingBeløpUtenRenter) {
            kladd.tilbakekrevingBeløpUtenRenter = tilbakekrevingBeløpUtenRenter;
            return this;
        }

        public Builder medRenterProsent(BigDecimal renterProsent) {
            kladd.renterProsent = renterProsent;
            return this;
        }

        public Builder medRenteBeløp(BigDecimal renteBeløp) {
            kladd.renteBeløp = renteBeløp;
            return this;
        }

        public Builder medSkattBeløp(BigDecimal skattBeløp) {
            kladd.skattBeløp = skattBeløp;
            return this;
        }

        public Builder medFeilutbetaltBeløp(BigDecimal feilutbetaltBeløp) {
            kladd.feilutbetaltBeløp = feilutbetaltBeløp;
            return this;
        }

        public Builder medUtbetaltYtelseBeløp(BigDecimal utbetaltYtelseBeløp) {
            kladd.utbetaltYtelseBeløp = utbetaltYtelseBeløp;
            return this;
        }

        public Builder medRiktigYtelseBeløp(BigDecimal riktigYtelseBeløp) {
            kladd.riktigYtelseBeløp = riktigYtelseBeløp;
            return this;
        }

        public BeregningResultatPeriode build() {
            Objects.requireNonNull(kladd.periode, "periode");
            Objects.requireNonNull(kladd.tilbakekrevingBeløp, "tilbakekrevingBeløp");
            Objects.requireNonNull(kladd.tilbakekrevingBeløpEtterSkatt, "tilbakekrevingBeløpEtterSkatt");
            Objects.requireNonNull(kladd.tilbakekrevingBeløpUtenRenter, "tilbakekrevingBeløpUtenRenter");
            Objects.requireNonNull(kladd.feilutbetaltBeløp, "periode");
            Objects.requireNonNull(kladd.renteBeløp, "renteBeløp");
            Objects.requireNonNull(kladd.skattBeløp, "skattBeløp");
            Objects.requireNonNull(this.erForeldet, "erForeldet");
            return kladd;
        }
    }

    public Periode getPeriode() {
        return periode;
    }

    public boolean erForeldet() {
        return erForeldet;
    }

    public BigDecimal getTilbakekrevingBeløp() {
        return tilbakekrevingBeløp;
    }

    public BigDecimal getTilbakekrevingBeløpEtterSkatt() {
        return tilbakekrevingBeløpEtterSkatt;
    }

    public BigDecimal getTilbakekrevingBeløpUtenRenter() {
        return tilbakekrevingBeløpUtenRenter;
    }

    public BigDecimal getRenterProsent() {
        return renterProsent;
    }

    public BigDecimal getRenteBeløp() {
        return renteBeløp;
    }

    public BigDecimal getSkattBeløp() {
        return skattBeløp;
    }

    public BigDecimal getFeilutbetaltBeløp() {
        return feilutbetaltBeløp;
    }

    public BigDecimal getUtbetaltYtelseBeløp() {
        return utbetaltYtelseBeløp;
    }

    public BigDecimal getRiktigYtelseBeløp() {
        return riktigYtelseBeløp;
    }

    public KodeResultat utledKodeResultat() {
        if (erForeldet) {
            return KodeResultat.FORELDET;
        }
        if (tilbakekrevingBeløpUtenRenter.signum() == 0) {
            return KodeResultat.INGEN_TILBAKEKREVING;
        }
        if (feilutbetaltBeløp.compareTo(tilbakekrevingBeløpUtenRenter) == 0) {
            return KodeResultat.FULL_TILBAKEKREVING;
        }
        return KodeResultat.DELVIS_TILBAKEKREVING;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BeregningResultatPeriode annen) {
            return Objects.equals(periode, annen.periode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }
}
