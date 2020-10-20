package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.transisjoner;


import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.StegTransisjon;

class Utført implements StegTransisjon {

    @Override
    public String getId() {
        return FellesTransisjoner.UTFØRT.getId();
    }

    @Override
    public BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg) {
        return nåværendeSteg.getBehandlingModell().finnNesteSteg(nåværendeSteg.getBehandlingStegType());
    }

    @Override
    public String toString() {
        return "Utført{}";
    }
}
