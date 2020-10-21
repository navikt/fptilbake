package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;


public class TestStegKonfig {
    private final BehandlingStegType behandlingStegType;
    private final BehandlingType behandlingType;
    private final BehandlingSteg steg;
    private final List<AksjonspunktDefinisjon> inngangAksjonspunkter;
    private final List<AksjonspunktDefinisjon> utgangAksjonspunkter;

    public TestStegKonfig(BehandlingStegType behandlingStegType, BehandlingType behandlingType, BehandlingSteg steg, List<AksjonspunktDefinisjon> inngangAksjonspunkter, List<AksjonspunktDefinisjon> utgangAksjonspunkter) {
        this.behandlingStegType = behandlingStegType;
        this.behandlingType = behandlingType;
        this.steg = steg;
        this.inngangAksjonspunkter = inngangAksjonspunkter;
        this.utgangAksjonspunkter = utgangAksjonspunkter;
    }

    public TestStegKonfig(BehandlingStegType behandlingStegType, BehandlingType behandlingType, BehandlingSteg steg) {
        this(behandlingStegType, behandlingType, steg, Collections.emptyList(), Collections.emptyList());
    }

    public BehandlingStegType getBehandlingStegType() {
        return behandlingStegType;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public BehandlingSteg getSteg() {
        return steg;
    }

    public List<AksjonspunktDefinisjon> getInngangAksjonspunkter() {
        return inngangAksjonspunkter;
    }

    public List<AksjonspunktDefinisjon> getUtgangAksjonspunkter() {
        return utgangAksjonspunkter;
    }
}
