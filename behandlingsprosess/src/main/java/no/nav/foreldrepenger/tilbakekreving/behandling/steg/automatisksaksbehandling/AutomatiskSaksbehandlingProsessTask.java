package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
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
@ProsessTask(AutomatiskSaksbehandlingProsessTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class AutomatiskSaksbehandlingProsessTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(AutomatiskSaksbehandlingProsessTask.class);
    public static final String TASKTYPE = "saksbehandling.automatisk";


    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    AutomatiskSaksbehandlingProsessTask() {
        // for CDI
    }

    @Inject
    public AutomatiskSaksbehandlingProsessTask(BehandlingRepositoryProvider repositoryProvider,
                                               BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        logger.info("Startet automatisk saksbehandling for behandling={}",behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        skruPåAutomatiskSaksbehandling(behandling);
        startAutomatiskSaksbehandling(behandling);
    }

    private void skruPåAutomatiskSaksbehandling(Behandling behandling) {
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandling.skruPåAutomatiskSaksbehandling();
        behandling.setAnsvarligSaksbehandler("VL");
        behandling.setAnsvarligBeslutter("VL");
        behandlingRepository.lagre(behandling, behandlingLås);
    }

    private void startAutomatiskSaksbehandling(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        //behandlingen er allerede i fakta, derfor tilbakefører behandlingen for å saksbehandle fakta steget automatisk
        behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, BehandlingStegType.FAKTA_VERGE);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
    }

}
