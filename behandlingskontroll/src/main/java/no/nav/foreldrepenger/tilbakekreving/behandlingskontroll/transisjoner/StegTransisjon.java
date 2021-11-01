package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;

public interface StegTransisjon {
    String getId();

    BehandlingStegModell nesteSteg(BehandlingStegModell nåværendeSteg);

    default Optional<BehandlingStegType> getMålstegHvisHopp() {
        return Optional.empty();
    }

    default BehandlingStegResultat getRetningForHopp() {
        throw new IllegalArgumentException("Utviklerfeil: skal ikke kalles for transisjon " + getId());
    }
}
