package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingEnhetEvent;
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
        beanManager.fireEvent(event, new Annotation[] {});
    }
}
