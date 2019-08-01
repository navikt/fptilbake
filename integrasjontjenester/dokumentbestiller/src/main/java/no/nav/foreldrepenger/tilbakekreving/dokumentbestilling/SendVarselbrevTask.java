package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
@ProsessTask(SendVarselbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendVarselbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.sendVarsel";

    private BestillDokumentTjeneste bestillDokumentTjeneste;

    @Inject
    public SendVarselbrevTask(BestillDokumentTjeneste bestillDokumentTjeneste) {
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        bestillDokumentTjeneste.sendVarselbrev(prosessTaskData.getFagsakId(), prosessTaskData.getAktørId(), prosessTaskData.getBehandlingId());
    }
}
