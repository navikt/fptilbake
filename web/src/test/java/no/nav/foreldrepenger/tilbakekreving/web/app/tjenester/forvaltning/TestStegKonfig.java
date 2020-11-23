package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;


public class TestStegKonfig {
    private final BehandlingStegType behandlingStegType;
    private final BehandlingType behandlingType;
    private final BehandlingSteg steg;


    public TestStegKonfig(BehandlingStegType behandlingStegType, BehandlingType behandlingType, BehandlingSteg steg) {
        this.behandlingStegType = behandlingStegType;
        this.behandlingType = behandlingType;
        this.steg = steg;
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

}
