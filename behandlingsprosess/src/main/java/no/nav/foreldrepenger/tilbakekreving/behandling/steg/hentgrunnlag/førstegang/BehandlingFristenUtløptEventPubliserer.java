package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingFristenUtløptEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

@ApplicationScoped
public class BehandlingFristenUtløptEventPubliserer {

    private BeanManager beanManager;

    BehandlingFristenUtløptEventPubliserer() {
        // for CDI
    }

    @Inject
    public BehandlingFristenUtløptEventPubliserer(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public void fireEvent(Behandling behandling, LocalDateTime fristDato) {
        BehandlingFristenUtløptEvent utløptEvent = new BehandlingFristenUtløptEvent(behandling, fristDato);
        beanManager.fireEvent(utløptEvent);
    }
}
