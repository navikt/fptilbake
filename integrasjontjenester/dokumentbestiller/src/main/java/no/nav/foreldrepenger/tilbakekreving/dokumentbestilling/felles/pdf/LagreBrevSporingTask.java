package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(LagreBrevSporingTask.TASKTYPE)
public class LagreBrevSporingTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.sporing";

    private HistorikkinnslagBrevTjeneste historikkinnslagBrevTjeneste;
    private BrevSporingTjeneste brevSporingTjeneste;

    public LagreBrevSporingTask() {
        //CDI proxy
    }

    @Inject
    public LagreBrevSporingTask(HistorikkinnslagBrevTjeneste historikkinnslagBrevTjeneste, BrevSporingTjeneste brevSporingTjeneste) {
        this.historikkinnslagBrevTjeneste = historikkinnslagBrevTjeneste;
        this.brevSporingTjeneste = brevSporingTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        BrevMottaker mottaker = BrevMottaker.valueOf(prosessTaskData.getPropertyValue("mottaker"));
        JournalpostIdOgDokumentId dokumentreferanse = finnJournalpostIdOgDokumentId(prosessTaskData);
        DetaljertBrevType brevType = DetaljertBrevType.valueOf(prosessTaskData.getPropertyValue("detaljertBrevType"));

        historikkinnslagBrevTjeneste.opprettHistorikkinnslagBrevSendt(behandlingId, dokumentreferanse, brevType, mottaker);
        brevSporingTjeneste.lagreInfoOmUtsendtBrev(behandlingId, dokumentreferanse, brevType);
    }

    private static JournalpostIdOgDokumentId finnJournalpostIdOgDokumentId(ProsessTaskData prosessTaskData) {
        String dokumentId = prosessTaskData.getPropertyValue("dokumentId");
        return new JournalpostIdOgDokumentId(finnJournalpostId(prosessTaskData), dokumentId);
    }

    private static JournalpostId finnJournalpostId(ProsessTaskData prosessTaskData) {
        String journalpostId = prosessTaskData.getPropertyValue("journalpostId");
        return new JournalpostId(journalpostId);
    }


}
