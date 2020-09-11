package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.SelvbetjeningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.Hendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SendBeskjedUtsendtVarselTilSelvbetjeningTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendBeskjedUtsendtVarselTilSelvbetjeningTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "send.beskjed.utsendt.varsel.selvbetjening";

    private static final Set<FagsakYtelseType> YTELSETYPER_STØTTET_I_SELVBETJENING = Set.of(FagsakYtelseType.FORELDREPENGER, FagsakYtelseType.SVANGERSKAPSPENGER, FagsakYtelseType.ENGANGSTØNAD);

    private SelvbetjeningTjeneste selvbetjeningTjeneste;

    SendBeskjedUtsendtVarselTilSelvbetjeningTask() {
        //for CDI proxy
    }

    @Inject
    public SendBeskjedUtsendtVarselTilSelvbetjeningTask(SelvbetjeningTjeneste selvbetjeningTjeneste) {
        this.selvbetjeningTjeneste = selvbetjeningTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        selvbetjeningTjeneste.sendMelding(behandlingId, Hendelse.TILBAKEKREVING_SPM);
    }

    public static boolean kanSendeVarsel(Behandling behandling) {
        return YTELSETYPER_STØTTET_I_SELVBETJENING.contains(behandling.getFagsak().getFagsakYtelseType());
    }
}
