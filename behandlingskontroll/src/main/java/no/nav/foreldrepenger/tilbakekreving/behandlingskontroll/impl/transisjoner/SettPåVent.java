package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.transisjoner;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.StegTransisjon;

class SettPåVent implements StegTransisjon {

    @Override
    public String getId() {
        return FellesTransisjoner.SETT_PÅ_VENT.getId();
    }

    @Override
    public BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg) {
        return nåværendeSteg;
    }

    @Override
    public String toString() {
        return "SettPåVent{}";
    }
}
