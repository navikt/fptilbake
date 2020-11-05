package no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.tjeneste.HendelseHåndtererTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.util.env.Environment;

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
        if (Environment.current().isProd()) {
            //FIXME midlertidig unngå prosesstasken pga produksjonshendelse i Kafka.
            //FIXME denne skal fikses så snart som mulig
            return;
        }


        HendelseTaskDataWrapper dataWrapper = new HendelseTaskDataWrapper(taskData);
        dataWrapper.validerTaskDataHåndterHendelse();

        hendelseHåndterer.håndterHendelse(dataWrapper);
    }
}
