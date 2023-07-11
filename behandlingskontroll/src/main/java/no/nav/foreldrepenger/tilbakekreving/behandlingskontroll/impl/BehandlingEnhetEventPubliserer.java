package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;


@ApplicationScoped
public class BehandlingEnhetEventPubliserer {

    private BeanManager beanManager;

    BehandlingEnhetEventPubliserer() {
        //for CDI proxy
    }

    @Inject
    public BehandlingEnhetEventPubliserer(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public void fireEvent(Behandling behandling) {
        BehandlingEnhetEvent event = new BehandlingEnhetEvent(behandling);
        beanManager.fireEvent(event, new Annotation[]{});
    }
}
