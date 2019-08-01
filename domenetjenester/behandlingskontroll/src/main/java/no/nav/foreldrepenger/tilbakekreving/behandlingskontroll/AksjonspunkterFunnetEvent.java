package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;

/**
 * Event fyres når et eller flere nye aksjonspunkter er funnet i et steg. Merk: håndterer mer enn et aksjonspunkt
 */
public class AksjonspunkterFunnetEvent extends AksjonspunktEvent {

    public AksjonspunkterFunnetEvent(BehandlingskontrollKontekst kontekst, List<Aksjonspunkt> aksjonspunkter,
            BehandlingStegType behandlingStegType) {
        super(kontekst, aksjonspunkter, behandlingStegType);
    }
}
