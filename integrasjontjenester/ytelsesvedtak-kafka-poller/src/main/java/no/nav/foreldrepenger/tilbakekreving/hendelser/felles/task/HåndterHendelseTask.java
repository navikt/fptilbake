package no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.tjeneste.HendelseHåndtererTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(HåndterHendelseTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class HåndterHendelseTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "hendelser.håndterHendelse";

    private HendelseHåndtererTjeneste hendelseHåndterer;

    HåndterHendelseTask() {
        // CDI
    }

    @Inject
    public HåndterHendelseTask(HendelseHåndtererTjeneste hendelseHåndterer) {
        this.hendelseHåndterer = hendelseHåndterer;
    }

    @Override
    public void doTask(ProsessTaskData taskData) {
        HendelseTaskDataWrapper dataWrapper = new HendelseTaskDataWrapper(taskData);
        dataWrapper.validerTaskDataHåndterHendelse();

        hendelseHåndterer.håndterHendelse(dataWrapper);
    }
}
