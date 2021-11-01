package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.observer;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingManglerKravgrunnlagFristenUtløptEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

@ApplicationScoped
public class BehandlingManglerKravgrunnlagFristenUtløptEventPubliserer {

    private BeanManager beanManager;

    BehandlingManglerKravgrunnlagFristenUtløptEventPubliserer() {
        // for CDI
    }

    @Inject
    public BehandlingManglerKravgrunnlagFristenUtløptEventPubliserer(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public void fireEvent(Behandling behandling, LocalDateTime fristDato) {
        BehandlingManglerKravgrunnlagFristenUtløptEvent utløptEvent = new BehandlingManglerKravgrunnlagFristenUtløptEvent(behandling, fristDato);
        beanManager.fireEvent(utløptEvent);
    }
}
