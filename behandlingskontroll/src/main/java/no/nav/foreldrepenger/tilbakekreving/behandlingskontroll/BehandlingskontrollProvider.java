package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;

@ApplicationScoped
public class BehandlingskontrollProvider {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;

    BehandlingskontrollProvider() {
        // CDI
    }

    @Inject
    public BehandlingskontrollProvider(BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                       BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
    }

    public BehandlingskontrollTjeneste getBehandlingskontrollTjeneste() {
        return behandlingskontrollTjeneste;
    }

    public BehandlingskontrollAsynkTjeneste getBehandlingskontrollAsynkTjeneste() {
        return behandlingskontrollAsynkTjeneste;
    }
}
