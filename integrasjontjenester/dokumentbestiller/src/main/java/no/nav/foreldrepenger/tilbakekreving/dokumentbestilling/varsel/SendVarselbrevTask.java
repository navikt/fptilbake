package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker.BRUKER;
import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker.VERGE;
import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker.VERGE_BRUKER;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(SendVarselbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendVarselbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.sendVarsel";

    private VarselbrevTjeneste varselbrevTjeneste;
    private VergeRepository vergeRepository;

    @Inject
    public SendVarselbrevTask(VarselbrevTjeneste varselbrevTjeneste,
                              VergeRepository vergeRepository) {
        this.varselbrevTjeneste = varselbrevTjeneste;
        this.vergeRepository = vergeRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(behandlingId);
        if(vergeEntitet.isPresent()){
            varselbrevTjeneste.sendVarselbrev(behandlingId, VERGE);
            varselbrevTjeneste.sendVarselbrev(behandlingId, VERGE_BRUKER);
        } else {
            varselbrevTjeneste.sendVarselbrev(behandlingId, BRUKER);
        }
    }
}
