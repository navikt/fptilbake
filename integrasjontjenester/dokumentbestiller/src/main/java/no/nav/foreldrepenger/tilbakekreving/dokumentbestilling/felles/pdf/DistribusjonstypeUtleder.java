package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist.Distribusjonstype;

class DistribusjonstypeUtleder {

    private DistribusjonstypeUtleder() {}

    static Distribusjonstype utledFor(BrevType brevType) {
        if (BrevType.VEDTAK_BREV.equals(brevType)
                || BrevType.FRITEKST.equals(brevType)) {
            return Distribusjonstype.VEDTAK;
        }
        return Distribusjonstype.VIKTIG;
    }
}
