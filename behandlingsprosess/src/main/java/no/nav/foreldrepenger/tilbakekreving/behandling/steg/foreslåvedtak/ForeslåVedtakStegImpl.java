package no.nav.foreldrepenger.tilbakekreving.behandling.steg.foreslåvedtak;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;


@BehandlingStegRef(kode = "FORVEDSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class ForeslåVedtakStegImpl implements ForeslåVedtakSteg {

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        return BehandleStegResultat.utførtMedAksjonspunkter(Collections.singletonList(AksjonspunktDefinisjon.FORESLÅ_VEDTAK));
    }

}
