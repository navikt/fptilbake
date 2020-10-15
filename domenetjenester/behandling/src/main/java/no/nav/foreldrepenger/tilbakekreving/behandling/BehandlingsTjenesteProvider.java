package no.nav.foreldrepenger.tilbakekreving.behandling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;

@ApplicationScoped
public class BehandlingsTjenesteProvider {

    private BehandlingTjeneste behandlingTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private BehandlingRevurderingTjeneste revurderingTjeneste;
    private BehandlendeEnhetTjeneste enhetTjeneste;

    BehandlingsTjenesteProvider() {
        // for CDI proxy
    }

    @Inject
    public BehandlingsTjenesteProvider(BehandlingTjeneste behandlingTjeneste, GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                                       HenleggBehandlingTjeneste henleggBehandlingTjeneste, BehandlingRevurderingTjeneste revurderingTjeneste,
                                       BehandlendeEnhetTjeneste enhetTjeneste) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.henleggBehandlingTjeneste = henleggBehandlingTjeneste;
        this.revurderingTjeneste = revurderingTjeneste;
        this.enhetTjeneste = enhetTjeneste;
    }

    public BehandlingTjeneste getBehandlingTjeneste() {
        return behandlingTjeneste;
    }

    public GjenopptaBehandlingTjeneste getGjenopptaBehandlingTjeneste() {
        return gjenopptaBehandlingTjeneste;
    }

    public HenleggBehandlingTjeneste getHenleggBehandlingTjeneste() {
        return henleggBehandlingTjeneste;
    }

    public BehandlingRevurderingTjeneste getRevurderingTjeneste() {
        return revurderingTjeneste;
    }

    public BehandlendeEnhetTjeneste getEnhetTjeneste() {
        return enhetTjeneste;
    }
}
