package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.observer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatusEventPubliserer;


@Dependent
public class OppdaterFagsakStatus {

    private BehandlingRepository behandlingRepository;
    private FagsakStatusEventPubliserer fagsakStatusEventPubliserer;
    private FagsakRepository fagsakRepository;

    OppdaterFagsakStatus() {
        // CDI
    }

    @Inject
    public OppdaterFagsakStatus(BehandlingRepository behandlingRepository,
                                FagsakRepository fagsakRepository,
                                FagsakStatusEventPubliserer fagsakStatusEventPubliserer) {
        this.behandlingRepository = behandlingRepository;
        this.fagsakRepository = fagsakRepository;
        this.fagsakStatusEventPubliserer = fagsakStatusEventPubliserer;
    }


    public void oppdaterFagsakNårBehandlingEndret(Behandling behandling) {
        oppdaterFagsak(behandling);
    }

    private void oppdaterFagsak(Behandling behandling) {

        if (Objects.equals(BehandlingStatus.AVSLUTTET, behandling.getStatus())) {
            avsluttFagsakNårAlleBehandlingerErLukket(behandling);
        } else {
            // hvis en Behandling har noe annen status, setter Fagsak til Under behandling
            oppdaterFagsakStatus(behandling, FagsakStatus.UNDER_BEHANDLING);
        }
    }

    private void avsluttFagsakNårAlleBehandlingerErLukket(Behandling behandling) {
        Long fagsakId = behandling.getFagsakId();
        List<Behandling> alleÅpneBehandlinger = behandlingRepository.hentBehandlingerSomIkkeErAvsluttetForFagsakId(fagsakId);

        Optional<Behandling> åpneBortsettFraAngitt = alleÅpneBehandlinger.stream()
                .filter(b -> !Objects.equals(behandling.getId(), b.getId()))
                .findAny();

        if (!åpneBortsettFraAngitt.isPresent()) {
            // ingen andre behandlinger er åpne
            oppdaterFagsakStatus(behandling, FagsakStatus.AVSLUTTET);
        }
    }

    void oppdaterFagsakStatus(Behandling behandling, FagsakStatus nyStatus) {
        Fagsak fagsak = behandling.getFagsak();
        FagsakStatus gammelStatus = fagsak.getStatus();
        Long fagsakId = fagsak.getId();
        fagsakRepository.oppdaterFagsakStatus(fagsakId, nyStatus);

        if (fagsakStatusEventPubliserer != null) {
            fagsakStatusEventPubliserer.fireEvent(fagsak, behandling, gammelStatus, nyStatus);
        }
    }
}
