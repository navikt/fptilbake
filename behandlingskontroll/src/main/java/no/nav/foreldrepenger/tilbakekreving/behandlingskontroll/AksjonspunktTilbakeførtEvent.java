package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;


public class AksjonspunktTilbakeførtEvent extends AksjonspunktEvent {

    public AksjonspunktTilbakeførtEvent(BehandlingskontrollKontekst kontekst, List<Aksjonspunkt> aksjonspunkter,
            BehandlingStegType behandlingStegType) {
        super(kontekst, aksjonspunkter, behandlingStegType);
    }
}
