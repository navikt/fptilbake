package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingManglerKravgrunnlagFristenEndretEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

@ApplicationScoped
public class BehandlingManglerKravgrunnlagFristenEndretEventPubliserer {

    private BeanManager beanManager;

    BehandlingManglerKravgrunnlagFristenEndretEventPubliserer() {
        // for CDI
    }

    @Inject
    public BehandlingManglerKravgrunnlagFristenEndretEventPubliserer(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public void fireEvent(Behandling behandling, LocalDateTime fristDato) {
        BehandlingManglerKravgrunnlagFristenEndretEvent utløptEvent = new BehandlingManglerKravgrunnlagFristenEndretEvent(behandling, fristDato);
        beanManager.fireEvent(utløptEvent);
    }
}
