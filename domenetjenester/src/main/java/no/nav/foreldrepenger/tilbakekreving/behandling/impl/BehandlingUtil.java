package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

class BehandlingUtil {

    private BehandlingUtil(){
    }

    static LocalDateTime bestemFristForBehandlingVent(LocalDate frist, Period defaultVentefrist) {
        return frist != null
            ? LocalDateTime.of(frist, LocalDateTime.now().toLocalTime())
            : LocalDateTime.now().plus(defaultVentefrist);
    }

}
