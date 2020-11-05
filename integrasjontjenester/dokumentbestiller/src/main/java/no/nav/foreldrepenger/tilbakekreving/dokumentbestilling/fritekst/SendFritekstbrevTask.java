package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekst;

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
@ProsessTask(SendFritekstbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendFritekstbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.fritekstbrev";

    private FritekstbrevTjeneste fritekstbrevTjeneste;
    private VergeRepository vergeRepository;

    @Inject
    public SendFritekstbrevTask(FritekstbrevTjeneste fritekstbrevTjeneste,
                                VergeRepository vergeRepository) {
        this.fritekstbrevTjeneste = fritekstbrevTjeneste;
        this.vergeRepository = vergeRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        String fritekst = prosessTaskData.getPayloadAsString();
        if (vergeRepository.finnesVerge(behandlingId)) {
            fritekstbrevTjeneste.sendFritekstbrev(behandlingId, fritekst, BrevMottaker.VERGE);
        }
        fritekstbrevTjeneste.sendFritekstbrev(behandlingId, fritekst, BrevMottaker.BRUKER);
    }
}
