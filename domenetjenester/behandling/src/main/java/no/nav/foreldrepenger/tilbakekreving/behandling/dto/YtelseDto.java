package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.math.BigDecimal;

public class YtelseDto {

    private String aktivitet;

    private BigDecimal belop;

    public String getAktivitet() {
        return aktivitet;
    }

    public BigDecimal getBelop() {
        return belop;
    }

    public void setAktivitet(String aktivitet) {
        this.aktivitet = aktivitet;
    }

    public void setBelop(BigDecimal beløp) {
        this.belop = beløp;
    }
}
