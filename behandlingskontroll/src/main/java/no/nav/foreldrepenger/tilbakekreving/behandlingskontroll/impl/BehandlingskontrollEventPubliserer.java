package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.lang.annotation.Annotation;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStegOvergangEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStegStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStegTilstandEndringEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;


/**
 * Håndterer fyring av events via CDI når det skjer en overgang i Behandlingskontroll mellom steg, eller statuser
 */
@ApplicationScoped
public class BehandlingskontrollEventPubliserer {

    public static final BehandlingskontrollEventPubliserer NULL_EVENT_PUB = new BehandlingskontrollEventPubliserer();

    private BeanManager beanManager;

    BehandlingskontrollEventPubliserer() {
        // null ctor, publiserer ingen events
    }

    @Inject
    public BehandlingskontrollEventPubliserer(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public void fireEvent(BehandlingStegOvergangEvent event) {
        var fraTilstand = event.getFraTilstand();
        var nyTilstand = event.getTilTilstand();
        if ((!fraTilstand.isPresent() && !nyTilstand.isPresent())
                || (fraTilstand.isPresent() && nyTilstand.isPresent() && Objects.equals(fraTilstand.get(), nyTilstand.get()))) {
            // ikke fyr duplikate events
            return;
        }

        doFireEvent(event);
    }

    public void fireEvent(BehandlingTransisjonEvent event) {
        doFireEvent(event);
    }

    public void fireEvent(BehandlingskontrollKontekst kontekst, BehandlingStegType stegType, BehandlingStegStatus forrigeStatus,
                          BehandlingStegStatus nyStatus) {
        if (Objects.equals(forrigeStatus, nyStatus)) {
            // gjør ingenting
            return;
        }
        doFireEvent(new BehandlingStegStatusEvent(kontekst, stegType, forrigeStatus, nyStatus));
    }

    public void fireEvent(BehandlingskontrollKontekst kontekst, BehandlingStatus gammelStatus, BehandlingStatus nyStatus) {
        if (Objects.equals(gammelStatus, nyStatus)) {
            // gjør ingenting
            return;
        }
        doFireEvent(BehandlingStatusEvent.nyEvent(kontekst, nyStatus));
    }

    public void fireEvent(BehandlingskontrollEvent event) {
        doFireEvent(event);
    }

    public void fireEvent(AksjonspunktStatusEvent event) {
        doFireEvent(event);
    }

    /**
     * Fyrer event via BeanManager slik at håndtering av events som subklasser andre events blir korrekt.
     */
    protected void doFireEvent(BehandlingEvent event) {
        if (beanManager == null) {
            return;
        }
        beanManager.fireEvent(event, new Annotation[]{});
    }

    public void fireEvent(BehandlingStegTilstandEndringEvent event) {
        doFireEvent(event);
    }
}
