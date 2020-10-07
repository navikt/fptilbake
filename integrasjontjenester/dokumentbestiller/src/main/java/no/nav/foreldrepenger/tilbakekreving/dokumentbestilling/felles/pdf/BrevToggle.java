package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.util.Objects;

import no.nav.vedtak.util.env.Environment;

public class BrevToggle {

    public static boolean brukDokprod() {
        return Environment.current().isProd() && "fptilbake".equals(Objects.requireNonNull(System.getProperty("application.name")));
    }
}
