package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.observer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;

/**
 * Observerer og propagerer / håndterer events internt i Behandlingskontroll
 */
@ApplicationScoped
public class FagsakStatusEventObserver {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private OppdaterFagsakStatus oppdaterFagsakStatus;
    private BehandlingRepository behandlingRepository;

    FagsakStatusEventObserver() {
        // For CDI
    }

    @Inject
    public FagsakStatusEventObserver(OppdaterFagsakStatus oppdaterFagsakStatus, BehandlingRepository behandlingRepository) {
        this.oppdaterFagsakStatus = oppdaterFagsakStatus;
        this.behandlingRepository = behandlingRepository;
    }

    public void observerBehandlingOpprettetEvent(@Observes BehandlingStatusEvent.BehandlingOpprettetEvent event) {
        log.debug("Oppdaterer status på Fagsak etter endring i behandling {}", event.getBehandlingId());//NOSONAR
        Behandling behandling = behandlingRepository.hentBehandling(event.getBehandlingId());
        oppdaterFagsakStatus.oppdaterFagsakNårBehandlingEndret(behandling);
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingAvsluttetEvent event) {
        log.debug("Oppdaterer status på Fagsak etter endring i behandling {}", event.getBehandlingId());//NOSONAR
        Behandling behandling = behandlingRepository.hentBehandling(event.getBehandlingId());
        oppdaterFagsakStatus.oppdaterFagsakNårBehandlingEndret(behandling);
    }
}
