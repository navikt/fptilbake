package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.simulering;

import java.time.LocalDate;

public class PeriodeDto {

    private LocalDate fom;
    private LocalDate tom;

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

}
