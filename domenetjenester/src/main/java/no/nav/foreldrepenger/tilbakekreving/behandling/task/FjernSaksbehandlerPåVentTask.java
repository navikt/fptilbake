package no.nav.foreldrepenger.tilbakekreving.behandling.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
@ProsessTask("oppgavebehandling.sbhpaavent")
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class FjernSaksbehandlerPåVentTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FjernSaksbehandlerPåVentTask.class);

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingRepository behandlingRepository;


    FjernSaksbehandlerPåVentTask() {
        // for CDI proxy
    }

    @Inject
    public FjernSaksbehandlerPåVentTask(BehandlingRepositoryProvider repositoryProvider,
                                        ProsessTaskTjeneste taskTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var behandlinger = behandlingRepository.finnÅpneBehandlinger();
        behandlinger.stream()
            .filter(b -> b.isBehandlingPåVent())
            .filter(b -> b.getAnsvarligSaksbehandler() != null)
            .forEach(beh -> {
                var lås = behandlingRepository.taSkriveLås(beh);
                beh.setAnsvarligSaksbehandler(null);
                behandlingRepository.lagre(beh, lås);
            });
    }
}
