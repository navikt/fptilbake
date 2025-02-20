package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.AvsluttBehandlingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "iverksetteVedtak.avsluttBehandling", prioritet = 2)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class AvsluttBehandlingTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AvsluttBehandlingTask.class);
    private AvsluttBehandlingTjeneste avsluttBehandlingTjeneste;

    AvsluttBehandlingTask() {
        // for CDI
    }

    @Inject
    public AvsluttBehandlingTask(AvsluttBehandlingTjeneste tjeneste) {
        this.avsluttBehandlingTjeneste = tjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        avsluttBehandlingTjeneste.avsluttBehandling(behandlingId);
        LOG.info("Utført for behandling: {}", behandlingId);
    }
}
