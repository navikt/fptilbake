package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class TilbakekrevingPeriode {
    private Periode periode;
    private BigDecimal renter = BigDecimal.ZERO;
    private List<TilbakekrevingBeløp> beløp = new ArrayList<>();

    protected TilbakekrevingPeriode(Periode periode) {
        this.periode = periode;
    }

    public static TilbakekrevingPeriode med(Periode periode) {
        return new TilbakekrevingPeriode(periode);
    }

    public TilbakekrevingPeriode medRenter(int renter) {
        this.renter = BigDecimal.valueOf(renter);
        return this;
    }

    public TilbakekrevingPeriode medRenter(BigDecimal renter) {
        this.renter = renter;
        return this;
    }

    public TilbakekrevingPeriode medBeløp(TilbakekrevingBeløp beløp) {
        this.beløp.add(beløp);
        return this;
    }

    public TilbakekrevingPeriode medBeløp(Collection<TilbakekrevingBeløp> beløp) {
        this.beløp.addAll(beløp);
        return this;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getRenter() {
        return renter;
    }

    public List<TilbakekrevingBeløp> getBeløp() {
        return beløp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TilbakekrevingPeriode) {
            TilbakekrevingPeriode that = (TilbakekrevingPeriode) o;
            return Objects.equals(periode, that.periode) &&
                    Objects.compare(renter, that.renter, BigDecimal::compareTo) == 0 &&
                    Objects.equals(beløp, that.beløp);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, renter);
    }

    @Override
    public String toString() {
        StringBuilder beløpBuilder = new StringBuilder("beløp=");
        for (TilbakekrevingBeløp b : beløp) {
            beløpBuilder.append("\n\t");
            beløpBuilder.append(b);
        }

        return "TilbakekrevingPeriode{" +
                "periode=" + periode +
                ", renter=" + renter +
                ", beløp=" + beløpBuilder +
                '}';
    }
}
