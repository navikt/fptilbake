package no.nav.foreldrepenger.tilbakekreving.behandling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;

@ApplicationScoped
public class BehandlingsTjenesteProvider {

    private BehandlingTjeneste behandlingTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingRevurderingTjeneste revurderingTjeneste;
    private BehandlendeEnhetTjeneste enhetTjeneste;

    BehandlingsTjenesteProvider() {
        // for CDI proxy
    }

    @Inject
    public BehandlingsTjenesteProvider(BehandlingTjeneste behandlingTjeneste, GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                                       BehandlingRevurderingTjeneste revurderingTjeneste,
                                       BehandlendeEnhetTjeneste enhetTjeneste) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.revurderingTjeneste = revurderingTjeneste;
        this.enhetTjeneste = enhetTjeneste;
    }

    public BehandlingTjeneste getBehandlingTjeneste() {
        return behandlingTjeneste;
    }

    public GjenopptaBehandlingTjeneste getGjenopptaBehandlingTjeneste() {
        return gjenopptaBehandlingTjeneste;
    }

    public BehandlingRevurderingTjeneste getRevurderingTjeneste() {
        return revurderingTjeneste;
    }

    public BehandlendeEnhetTjeneste getEnhetTjeneste() {
        return enhetTjeneste;
    }
}
