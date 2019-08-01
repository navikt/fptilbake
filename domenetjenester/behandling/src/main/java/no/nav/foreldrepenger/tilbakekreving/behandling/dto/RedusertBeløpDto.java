package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.math.BigDecimal;

public class RedusertBeløpDto {

    private boolean erTrekk;
    private BigDecimal belop;

    public boolean isErTrekk() {
        return erTrekk;
    }

    public BigDecimal getBelop() {
        return belop;
    }

    public void setErTrekk(boolean erTrekk) {
        this.erTrekk = erTrekk;
    }

    public void setBelop(BigDecimal belop) {
        this.belop = belop;
    }
}