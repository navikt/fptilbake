package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.util.Objects;

import no.nav.vedtak.util.env.Environment;

public class BrevToggle {

    public static boolean brukDokprod() {
        boolean erFptilbake = "fptilbake".equals(Objects.requireNonNull(System.getProperty("application.name")));
        return erFptilbake && Environment.current().isProd();
    }
}
