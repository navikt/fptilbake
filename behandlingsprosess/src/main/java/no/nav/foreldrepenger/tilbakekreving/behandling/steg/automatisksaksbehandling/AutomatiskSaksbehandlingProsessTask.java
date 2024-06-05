package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "saksbehandling.automatisk", prioritet = 2)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class AutomatiskSaksbehandlingProsessTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AutomatiskSaksbehandlingProsessTask.class);


    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    AutomatiskSaksbehandlingProsessTask() {
        // for CDI
    }

    @Inject
    public AutomatiskSaksbehandlingProsessTask(BehandlingRepository behandlingRepository,
                                               BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        LOG.info("Startet automatisk saksbehandling for behandling={}", behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (behandling.isBehandlingPåVent()) {
            LOG.warn("Behandling={} er på vent, kan ikke saksbehandle automatisk", behandlingId);
        } else if (behandling.erAvsluttet()) {
            LOG.warn("Behandling={} er allerede avsluttet, kan ikke saksbehandle automatisk", behandlingId);
        } else if (behandling.getAnsvarligSaksbehandler() != null && !behandling.getAnsvarligSaksbehandler().isEmpty()) {
            LOG.warn("Behandling={} er allerede saksbehandlet, kan ikke saksbehandle automatisk", behandlingId);
        } else {
            skruPåAutomatiskSaksbehandling(behandling);
            startAutomatiskSaksbehandling(behandling);
        }
    }

    private void skruPåAutomatiskSaksbehandling(Behandling behandling) {
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandling.skruPåAutomatiskSaksbehandlingPgaInnkrevingAvLavtBeløp();
        behandling.setAnsvarligSaksbehandler("VL");
        behandlingRepository.lagre(behandling, behandlingLås);
    }

    private void startAutomatiskSaksbehandling(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        //behandlingen er allerede i fakta, derfor tilbakefører behandlingen for å saksbehandle fakta steget automatisk
        behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, BehandlingStegType.FAKTA_VERGE);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
    }

}
