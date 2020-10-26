package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.vedtak.util.env.Environment;

public class BrevToggle {

    public static boolean brukDokprod(BrevType brevType) {
        return Environment.current().isProd()
            && "fptilbake".equals(Objects.requireNonNull(System.getProperty("application.name")))
            && !BrevType.VARSEL_BREV.equals(brevType);
    }
}
