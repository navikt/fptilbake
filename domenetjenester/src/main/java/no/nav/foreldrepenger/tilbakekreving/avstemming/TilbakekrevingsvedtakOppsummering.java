package no.nav.foreldrepenger.tilbakekreving.avstemming;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusEntitet;

public class TilbakekrevingsvedtakOppsummering {
    private String økonomiVedtakId;
    private BigDecimal tilbakekrevesBruttoUtenRenter;
    private BigDecimal tilbakekrevesNettoUtenRenter;
    private BigDecimal renter;
    private BigDecimal skatt;

    public static TilbakekrevingsvedtakOppsummering oppsummer(OppdragIverksettingStatusEntitet oppdragIverksettingStatusEntitet, BeregningsresultatEntitet beregningsresultat) {
        BigDecimal bruttoUtenRenter = BigDecimal.ZERO;
        BigDecimal renter = BigDecimal.ZERO;
        BigDecimal skatt = BigDecimal.ZERO;

        for (BeregningsresultatPeriodeEntitet periode : beregningsresultat.getPerioder()) {
            renter = renter.add(periode.getRenteBeløp());
            bruttoUtenRenter = bruttoUtenRenter.add(periode.getTilbakekrevingBeløpUtenRenter());
            skatt = skatt.add(periode.getSkattBeløp());
        }

        return new TilbakekrevingsvedtakOppsummering.Builder()
            .medRenter(renter)
            .medSkatt(skatt)
            .medTilbakekrevesBruttoUtenRenter(bruttoUtenRenter)
            .medTilbakekrevesNettoUtenRenter(bruttoUtenRenter.subtract(skatt))
            .medØkonomiVedtakId(oppdragIverksettingStatusEntitet.getVedtakId())
            .build();
    }

    public String getØkonomiVedtakId() {
        return økonomiVedtakId;
    }

    public BigDecimal getTilbakekrevesBruttoUtenRenter() {
        return tilbakekrevesBruttoUtenRenter;
    }

    public BigDecimal getTilbakekrevesNettoUtenRenter() {
        return tilbakekrevesNettoUtenRenter;
    }

    public BigDecimal getRenter() {
        return renter;
    }

    public BigDecimal getSkatt() {
        return skatt;
    }

    public boolean harIngenTilbakekreving() {
        return tilbakekrevesBruttoUtenRenter.signum() == 0;
    }

    public static class Builder {
        private boolean brukt;
        private TilbakekrevingsvedtakOppsummering kladd = new TilbakekrevingsvedtakOppsummering();

        public Builder medØkonomiVedtakId(BigInteger økonomiVedtakId) {
            kladd.økonomiVedtakId = økonomiVedtakId.toString();
            return this;
        }

        public Builder medØkonomiVedtakId(String økonomiVedtakId) {
            kladd.økonomiVedtakId = økonomiVedtakId;
            return this;
        }

        public Builder medTilbakekrevesBruttoUtenRenter(BigDecimal tilbakekrevesBruttoUtenRenter) {
            kladd.tilbakekrevesBruttoUtenRenter = tilbakekrevesBruttoUtenRenter;
            return this;
        }

        public Builder medTilbakekrevesNettoUtenRenter(BigDecimal tilbakekrevesNettoUtenRenter) {
            kladd.tilbakekrevesNettoUtenRenter = tilbakekrevesNettoUtenRenter;
            return this;
        }

        public Builder medRenter(BigDecimal renter) {
            kladd.renter = renter;
            return this;
        }

        public Builder medSkatt(BigDecimal skatt) {
            kladd.skatt = skatt;
            return this;
        }

        public TilbakekrevingsvedtakOppsummering build() {
            if (brukt) {
                throw new IllegalArgumentException("this.build() er allerede brukt, lag ny builder!");
            }
            Objects.requireNonNull(kladd.økonomiVedtakId, "mangler økonomiVedtakId");
            Objects.requireNonNull(kladd.tilbakekrevesBruttoUtenRenter, "mangler tilbakekrevesBruttoUtenRenter");
            Objects.requireNonNull(kladd.tilbakekrevesNettoUtenRenter, "mangler tilbakekrevesNettoUtenRenter");
            Objects.requireNonNull(kladd.renter, "mangler renter");
            Objects.requireNonNull(kladd.skatt, "mangler skatt");
            brukt = true;
            return kladd;
        }
    }
}
