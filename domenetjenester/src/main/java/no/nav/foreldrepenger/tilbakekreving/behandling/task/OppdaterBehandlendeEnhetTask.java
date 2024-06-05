package no.nav.foreldrepenger.tilbakekreving.behandling.task;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "oppgavebehandling.oppdaterEnhet", prioritet = 4)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class OppdaterBehandlendeEnhetTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OppdaterBehandlendeEnhetTask.class);

    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingTjeneste behandlingTjeneste;

    OppdaterBehandlendeEnhetTask() {
        // for CDI proxy
    }

    @Inject
    public OppdaterBehandlendeEnhetTask(BehandlingRepositoryProvider repositoryProvider,
                                        BehandlingTjeneste behandlingTjeneste,
                                        BehandlendeEnhetTjeneste behandlendeEnhetTjeneste) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.behandlendeEnhetTjeneste = behandlendeEnhetTjeneste;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        var eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        var enhet = behandlingTjeneste.hentEnhetForEksternBehandling(eksternBehandling.getEksternUuid());

        if (!enhet.equals(behandling.getBehandlendeOrganisasjonsEnhet())) {
            LOG.info("Endrer behandlende enhet for behandling: {}", prosessTaskData.getBehandlingId());
            behandlendeEnhetTjeneste.byttBehandlendeEnhet(behandling.getId(), enhet, HistorikkAktør.VEDTAKSLØSNINGEN);
        }
    }
}
