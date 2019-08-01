package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

public class InternalAksjonspunktManipulator {

    public void forceFristForAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, LocalDateTime fristTid) {
        behandling.getAksjonspunkter().stream()
                .filter(o -> aksjonspunktDefinisjon.equals(o.getAksjonspunktDefinisjon()))
                .forEach(o -> o.setFristTid(fristTid));
    }

}
