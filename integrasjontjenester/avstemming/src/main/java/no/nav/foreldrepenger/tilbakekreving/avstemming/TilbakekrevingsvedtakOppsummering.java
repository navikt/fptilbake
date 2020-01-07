package no.nav.foreldrepenger.tilbakekreving.avstemming;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsbelopDto;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsperiodeDto;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;

public class TilbakekrevingsvedtakOppsummering {
    private String økonomiVedtakId;
    private BigDecimal tilbakekrevesBruttoUtenRenter;
    private BigDecimal tilbakekrevesNettoUtenRenter;
    private BigDecimal renter;
    private BigDecimal skatt;

    public static TilbakekrevingsvedtakOppsummering oppsummer(TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        BigDecimal bruttoUtenRenter = BigDecimal.ZERO;
        BigDecimal renter = BigDecimal.ZERO;
        BigDecimal skatt = BigDecimal.ZERO;
        for (TilbakekrevingsperiodeDto periode : tilbakekrevingsvedtak.getTilbakekrevingsperiode()) {
            renter = renter.add(periode.getBelopRenter());
            for (TilbakekrevingsbelopDto beløp : periode.getTilbakekrevingsbelop()) {
                bruttoUtenRenter = bruttoUtenRenter.add(beløp.getBelopTilbakekreves());
                skatt = skatt.add(beløp.getBelopSkatt());
            }
        }

        TilbakekrevingsvedtakOppsummering tilbakekrevingsvedtakOppsummering = new TilbakekrevingsvedtakOppsummering.Builder()
            .medRenter(renter)
            .medSkatt(skatt)
            .medTilbakekrevesBruttoUtenRenter(bruttoUtenRenter)
            .medTilbakekrevesNettoUtenRenter(bruttoUtenRenter.subtract(skatt))
            .medØkonomiVedtakId(tilbakekrevingsvedtak.getVedtakId())
            .build();
        return tilbakekrevingsvedtakOppsummering;
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

    public static class Builder {
        private boolean brukt;
        private TilbakekrevingsvedtakOppsummering kladd = new TilbakekrevingsvedtakOppsummering();

        public Builder medØkonomiVedtakId(BigInteger økonomiVedtakId) {
            kladd.økonomiVedtakId = økonomiVedtakId.toString();
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
