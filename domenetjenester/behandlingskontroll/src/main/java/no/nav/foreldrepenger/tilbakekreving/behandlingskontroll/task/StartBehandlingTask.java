package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task;

import static no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.StartBehandlingTask.TASKTYPE;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * Kjører behandlingskontroll automatisk fra start.
 */
@Dependent
@ProsessTask(TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class StartBehandlingTask implements ProsessTaskHandler {
    public static final String TASKTYPE = "behandlingskontroll.startBehandling";

    @Inject
    public StartBehandlingTask() {
    }

    @Override
    public void doTask(ProsessTaskData data) {

        // dynamisk lookup
        CDI<Object> cdi = CDI.current();
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = cdi.select(BehandlingskontrollTjeneste.class).get();

        try {
            Long behandlingId = ProsessTaskDataWrapper.wrap(data).getBehandlingId();
            BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);

            // TODO (FC): assert at behandlingen starter fra første steg?
            behandlingskontrollTjeneste.prosesserBehandling(kontekst);
        } finally {
            cdi.destroy(behandlingskontrollTjeneste);
        }
    }
}
