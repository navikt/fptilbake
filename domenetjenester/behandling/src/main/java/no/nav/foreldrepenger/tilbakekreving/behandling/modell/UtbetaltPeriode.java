package no.nav.foreldrepenger.tilbakekreving.behandling.modell;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UtbetaltPeriode {

    private LocalDate fom;
    private LocalDate tom;
    private BigDecimal belop;
    private HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto;

    private UtbetaltPeriode() {
        // bruk statisk metode for å lage utbetalt periode
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public BigDecimal getBelop() {
        return belop;
    }

    public Periode tilPeriode() {
        return Periode.of(fom, tom);
    }

    public HendelseTypeMedUndertypeDto getFeilutbetalingÅrsakDto() {
        return feilutbetalingÅrsakDto;
    }

    public void setFeilutbetalingÅrsakDto(HendelseTypeMedUndertypeDto feilutbetalingÅrsakDto) {
        this.feilutbetalingÅrsakDto = feilutbetalingÅrsakDto;
    }

    public static UtbetaltPeriode lagPeriode(LocalDate fom, LocalDate tom, BigDecimal belop) {
        if (fom == null || tom == null) {
            throw new IllegalStateException("utviklerfeil: ikke-komplett periode - sjekk at fom og tom er gyldige verdier");
        } else if (fom.isAfter(tom)) {
            throw new IllegalStateException("fra og med dato er etter til og med dato");
        }
        UtbetaltPeriode periode = new UtbetaltPeriode();
        periode.fom = fom;
        periode.tom = tom;
        periode.belop = belop;
        return periode;
    }
}
