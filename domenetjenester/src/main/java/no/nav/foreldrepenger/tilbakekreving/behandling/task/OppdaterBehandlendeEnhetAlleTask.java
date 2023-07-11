package no.nav.foreldrepenger.tilbakekreving.behandling.task;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask("oppgavebehandling.nasjonalenhet")
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class OppdaterBehandlendeEnhetAlleTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OppdaterBehandlendeEnhetAlleTask.class);
    private static final String ENHET_KEY = "enhetId";

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingRepository behandlingRepository;


    OppdaterBehandlendeEnhetAlleTask() {
        // for CDI proxy
    }

    @Inject
    public OppdaterBehandlendeEnhetAlleTask(BehandlingRepositoryProvider repositoryProvider,
                                            ProsessTaskTjeneste taskTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var forenhet = Optional.ofNullable(prosessTaskData.getPropertyValue(ENHET_KEY)).orElseThrow();
        var behandlinger = behandlingRepository.finnBehandlingerIkkeAvsluttetPåAngittEnhet(forenhet);
        behandlinger.forEach(beh -> {
            var taskData = ProsessTaskData.forProsessTask(OppdaterBehandlendeEnhetTask.class);
            taskData.setBehandling(beh.getFagsakId(), beh.getId(), beh.getAktørId().getId());
            taskData.setCallIdFraEksisterende();
            taskTjeneste.lagre(taskData);
        });
    }
}
