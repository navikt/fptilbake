package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist.DokdistKlient;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(PubliserJournalpostTask.TASKTYPE)
public class PubliserJournalpostTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "publiser.journalpost.dokdist";

    private DokdistKlient dokdistKlient;

    private HistorikkinnslagBrevTjeneste historikkinnslagBrevTjeneste;
    private BrevSporingTjeneste brevSporingTjeneste;
    private VarselRepository varselRepository;

    public PubliserJournalpostTask() {
        //CDI proxy
    }

    @Inject
    public PubliserJournalpostTask(DokdistKlient dokdistKlient, HistorikkinnslagBrevTjeneste historikkinnslagBrevTjeneste, BrevSporingTjeneste brevSporingTjeneste, VarselRepository varselRepository) {
        this.dokdistKlient = dokdistKlient;
        this.historikkinnslagBrevTjeneste = historikkinnslagBrevTjeneste;
        this.brevSporingTjeneste = brevSporingTjeneste;
        this.varselRepository = varselRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        JournalpostId journalpostId = finnJournalpostId(prosessTaskData);
        BrevMottaker mottaker = BrevMottaker.valueOf(prosessTaskData.getPropertyValue("mottaker"));

        dokdistKlient.distribuerJournalpost(journalpostId, mottaker);

        //TODO vurder å flytte kode for historikkinslag og sporing til egen prosesstask
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        JournalpostIdOgDokumentId dokumentreferanse = finnJournalpostIdOgDokumentId(prosessTaskData);
        DetaljertBrevType brevType = DetaljertBrevType.valueOf(prosessTaskData.getPropertyValue("detaljertBrevType"));
        historikkinnslagBrevTjeneste.opprettHistorikkinnslagBrevSendt(behandlingId, dokumentreferanse, brevType, mottaker);
        brevSporingTjeneste.lagreInfoOmUtsendtBrev(behandlingId, dokumentreferanse, brevType);
        if (brevType.gjelderVarsel()) {
            //TODO vurder egen løsning for varsel, for å slippe spesialkode i generell prosesstask
            lagreVarsletBeløp(behandlingId, Long.valueOf(prosessTaskData.getPropertyValue("varseltBeloep")));
        }
    }

    private void lagreVarsletBeløp(Long behandlingId, Long varseltBeløp) {
        varselRepository.lagreVarseltBeløp(behandlingId, varseltBeløp);
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
