package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import no.nav.vedtak.exception.TekniskException;

public class BehandlingskontrollFeil {

    static TekniskException kanIkkeHenleggeAvsluttetBehandling(Long behandlingId) {
        return new TekniskException("FPT-143308", String.format("BehandlingId %s er allerede avsluttet, kan ikke henlegges", behandlingId));
    }

    static TekniskException kanIkkeGjenopptaBehandlingFantFlereAksjonspunkterSomMedførerTilbakehopp(Long behandlingId) {
        return new TekniskException("FPT-105126", String.format("BehandlingId %s har flere enn et aksjonspunkt, hvor aksjonspunktet fører til tilbakehopp ved gjenopptakelse. Kan ikke gjenopptas.", behandlingId));
    }

}
