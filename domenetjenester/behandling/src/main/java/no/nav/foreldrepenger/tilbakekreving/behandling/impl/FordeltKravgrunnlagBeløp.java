package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.math.BigDecimal;

public class FordeltKravgrunnlagBeløp {
    private BigDecimal feilutbetaltBeløp;
    private BigDecimal utbetaltYtelseBeløp;
    private BigDecimal riktigYtelseBeløp;

    public FordeltKravgrunnlagBeløp(BigDecimal feilutbetaltBeløp, BigDecimal utbetaltYtelseBeløp, BigDecimal riktigBeløp) {
        this.feilutbetaltBeløp = feilutbetaltBeløp;
        this.utbetaltYtelseBeløp = utbetaltYtelseBeløp;
        this.riktigYtelseBeløp = riktigBeløp;
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
}
