package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.selvbetjening;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SendBeskjedUtsendtVarselTilSelvbetjeningTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendBeskjedUtsendtVarselTilSelvbetjeningTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "send.beskjed.utsendt.varsel.selvbetjening";

    private VarselSelvbetjeningTjeneste varselSelvbetjeningTjeneste;

    SendBeskjedUtsendtVarselTilSelvbetjeningTask() {
        //for CDI proxy
    }

    @Inject
    public SendBeskjedUtsendtVarselTilSelvbetjeningTask(VarselSelvbetjeningTjeneste varselSelvbetjeningTjeneste) {
        this.varselSelvbetjeningTjeneste = varselSelvbetjeningTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        varselSelvbetjeningTjeneste.sendBeskjedOmUtsendtVarsel(behandlingId);
    }
}
