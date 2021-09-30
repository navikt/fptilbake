package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.AvsluttBehandlingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("iverksetteVedtak.avsluttBehandling")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class AvsluttBehandlingTask implements ProsessTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(AvsluttBehandlingTask.class);
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
        log.info("Utført for behandling: {}", behandlingId);
    }
}
