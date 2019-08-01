package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
