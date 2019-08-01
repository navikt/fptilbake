package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class PeriodeMedBeløp {
    private Periode periode;
    private BigDecimal beløp;

    public PeriodeMedBeløp(Periode periode, BigDecimal beløp) {
        this.periode = periode;
        this.beløp = beløp;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }
}
