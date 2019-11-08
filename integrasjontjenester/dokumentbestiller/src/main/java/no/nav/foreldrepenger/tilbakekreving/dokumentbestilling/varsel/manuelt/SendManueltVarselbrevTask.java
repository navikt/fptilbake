package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(SendManueltVarselbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendManueltVarselbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.sendManueltVarsel";

    private ManueltVarselBrevTjeneste manueltVarselBrevTjeneste;

    @Inject
    public SendManueltVarselbrevTask(ManueltVarselBrevTjeneste manueltVarselBrevTjeneste) {
        this.manueltVarselBrevTjeneste = manueltVarselBrevTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        DokumentMalType malType = DokumentMalType.fraKode(prosessTaskData.getPropertyValue(TaskProperty.MAL_TYPE));
        String friTekst = prosessTaskData.getPropertyValue(TaskProperty.FRITEKST);

        manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, malType, friTekst);
    }
}
