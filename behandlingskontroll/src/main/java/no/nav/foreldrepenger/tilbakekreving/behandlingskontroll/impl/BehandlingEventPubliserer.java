package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;


@ApplicationScoped
public class BehandlingEventPubliserer {

    private Event<BehandlingEvent> eventHandler;

    BehandlingEventPubliserer() {
        //for CDI proxy
    }

    @Inject
    public BehandlingEventPubliserer(Event<BehandlingEvent> eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void fireEvent(BehandlingEvent event) {
        if (eventHandler != null && event != null) {
            eventHandler.fire(event);
        }
    }
}
