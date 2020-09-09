package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(AutomatiskSaksbehandlingProsessTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class AutomatiskSaksbehandlingProsessTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(AutomatiskSaksbehandlingProsessTask.class);
    public static final String TASKTYPE = "saksbehandling.automatisk";

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        //TODO
    }
}
