package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.math.BigDecimal;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class GrunnlagPeriodeMedSkattProsent {

    private Periode periode;
    private BigDecimal tilbakekrevesBeløp;
    private BigDecimal skattProsent;

    public GrunnlagPeriodeMedSkattProsent(Periode periode, BigDecimal tilbakekrevesBeløp, BigDecimal skattProsent) {
        this.periode = periode;
        this.tilbakekrevesBeløp = tilbakekrevesBeløp;
        this.skattProsent = skattProsent;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getTilbakekrevesBeløp() {
        return tilbakekrevesBeløp;
    }

    public BigDecimal getSkattProsent() {
        return skattProsent;
    }
}
