package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(SendHenleggelsesbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendHenleggelsesbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.sendhenleggelse";

    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste;
    private VergeRepository vergeRepository;

    @Inject
    public SendHenleggelsesbrevTask(HenleggelsesbrevTjeneste henleggelsesbrevTjeneste,
                                    VergeRepository vergeRepository) {
        this.henleggelsesbrevTjeneste = henleggelsesbrevTjeneste;
        this.vergeRepository = vergeRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        if(vergeRepository.finnesVerge(behandlingId)){
            henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, BrevMottaker.VERGE);
        }
        henleggelsesbrevTjeneste.sendHenleggelsebrev(behandlingId, BrevMottaker.BRUKER);
    }
}
