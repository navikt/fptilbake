package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

import no.nav.foreldrepenger.integrasjon.dokument.felles.SpraakkodeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;

public class BrevSpråkUtil {

    private BrevSpråkUtil() {
        // gjem implisitt konstruktør
    }

    public static SpraakkodeType mapSpråkkode(Språkkode språkkode) {
        return SpraakkodeType.fromValue(språkkode.getKode());
    }

}
