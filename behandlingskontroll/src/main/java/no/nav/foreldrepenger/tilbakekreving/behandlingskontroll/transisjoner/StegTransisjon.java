package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;

public interface StegTransisjon {
    String getId();

    BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg);

    default boolean erFremoverhopp() {
        return false;
    }
}
