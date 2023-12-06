package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;


@ApplicationScoped
public class BehandlingEnhetEventPubliserer {

    private Event<BehandlingEnhetEvent> eventHandler;

    BehandlingEnhetEventPubliserer() {
        //for CDI proxy
    }

    @Inject
    public BehandlingEnhetEventPubliserer(Event<BehandlingEnhetEvent> eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void fireEvent(Behandling behandling) {
        BehandlingEnhetEvent event = new BehandlingEnhetEvent(behandling);
        eventHandler.fire(event);
    }
}
