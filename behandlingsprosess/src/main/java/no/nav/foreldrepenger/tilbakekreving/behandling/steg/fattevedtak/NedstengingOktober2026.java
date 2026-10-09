package no.nav.foreldrepenger.tilbakekreving.behandling.steg.fattevedtak;

import java.time.LocalDateTime;
import java.time.Month;

import no.nav.foreldrepenger.konfig.Environment;

public class NedstengingOktober2026 {

    private static final Environment ENV = Environment.current();
    private static final LocalDateTime FOM = LocalDateTime.of(2026, Month.OCTOBER, 9, 15, 50);
    private static final LocalDateTime TOM = LocalDateTime.of(2026, Month.OCTOBER, 19, 9, 0);

    private NedstengingOktober2026() {
        // for CDI proxy
    }

    public static boolean kanFatteVedtak() {
        if (ENV.isProd()) {
            LocalDateTime now = LocalDateTime.now();
            return now.isBefore(FOM) || now.isAfter(TOM);
        }
        return true;
    }

}
