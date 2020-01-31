package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

class BehandlingUtil {

    BehandlingUtil(){
        // for CDI proxy
    }

    static LocalDateTime bestemFristForBehandlingVent(LocalDate frist, Period defaultVentefrist) {
        return frist != null
            ? LocalDateTime.of(frist, LocalDateTime.now().toLocalTime())
            : LocalDateTime.now().plus(defaultVentefrist);
    }

    static boolean sjekkAvvikHvisSisteDagIHelgen(LocalDate sisteDag, int antallDager) {
        if (antallDager == 3 && sisteDag.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return false;
        }
        return antallDager != 2 || sisteDag.getDayOfWeek() != DayOfWeek.SATURDAY;
    }

}
