package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;

class DummyVenterSteg extends DummySteg {

    public DummyVenterSteg() {
        super();
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        BehandleStegResultat settPåVent = BehandleStegResultat.settPåVent();
        sisteUtførStegResultat.set(settPåVent);
        return settPåVent;
    }

    @Override
    public BehandleStegResultat gjenopptaSteg(BehandlingskontrollKontekst kontekst) {
        BehandleStegResultat resultat = BehandleStegResultat.utførtUtenAksjonspunkter();;
        sisteUtførStegResultat.set(resultat);
        return resultat;
    }

}
