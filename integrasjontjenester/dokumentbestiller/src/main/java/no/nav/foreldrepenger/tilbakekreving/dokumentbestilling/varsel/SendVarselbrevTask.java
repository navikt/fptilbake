package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(SendVarselbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendVarselbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.sendVarsel";

    private VarselbrevTjeneste varselbrevTjeneste;

    @Inject
    public SendVarselbrevTask(VarselbrevTjeneste varselbrevTjeneste) {
        this.varselbrevTjeneste = varselbrevTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        varselbrevTjeneste.sendVarselbrev(behandlingId);
    }
}
