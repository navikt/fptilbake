package no.nav.foreldrepenger.tilbakekreving.behandling.modell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class LogiskPeriode {

    private Periode periode;
    private BigDecimal feilutbetaltBeløp;

    public LogiskPeriode(Periode periode, BigDecimal feilutbetaltBeløp) {
        Objects.requireNonNull(periode, "periode er null");
        Objects.requireNonNull(feilutbetaltBeløp, "feilutbetalt beløp er null");
        this.periode = periode;
        this.feilutbetaltBeløp = feilutbetaltBeløp;
    }

    public Periode getPeriode() {
        return periode;
    }

    public LocalDate getFom() {
        return periode.getFom();
    }

    public LocalDate getTom() {
        return periode.getTom();
    }

    public BigDecimal getFeilutbetaltBeløp() {
        return feilutbetaltBeløp;
    }

    public static LogiskPeriode lagPeriode(LocalDate fom, LocalDate tom, BigDecimal feilutbetaltBeløp) {
        return lagPeriode(Periode.of(fom, tom), feilutbetaltBeløp);
    }

    public static LogiskPeriode lagPeriode(Periode periode, BigDecimal feilutbetaltBeløp) {
        return new LogiskPeriode(periode, feilutbetaltBeløp);
    }
}
