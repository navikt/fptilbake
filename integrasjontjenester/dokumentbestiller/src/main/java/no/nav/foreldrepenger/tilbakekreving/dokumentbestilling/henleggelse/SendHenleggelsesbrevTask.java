package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(SendHenleggelsesbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendHenleggelsesbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.sendhenleggelse";

    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste;

    @Inject
    public SendHenleggelsesbrevTask(HenleggelsesbrevTjeneste henleggelsesbrevTjeneste) {
        this.henleggelsesbrevTjeneste = henleggelsesbrevTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId);
    }
}
