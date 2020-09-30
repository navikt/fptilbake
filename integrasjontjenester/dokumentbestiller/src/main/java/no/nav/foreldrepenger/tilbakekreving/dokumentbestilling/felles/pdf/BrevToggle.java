package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import no.nav.vedtak.util.env.Environment;

public class BrevToggle {

    public static boolean brukDokprod() {
        return Environment.current().isProd();
    }
}
