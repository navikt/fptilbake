package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist.DokdistKlient;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(PubliserJournalpostTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class PubliserJournalpostTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "publiser.journalpost.dokdist";

    private DokdistKlient dokdistKlient;

    public PubliserJournalpostTask() {
        //CDI proxy
    }

    @Inject
    public PubliserJournalpostTask(DokdistKlient dokdistKlient) {
        this.dokdistKlient = dokdistKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        JournalpostId journalpostId = finnJournalpostId(prosessTaskData);
        BrevMottaker mottaker = BrevMottaker.valueOf(prosessTaskData.getPropertyValue("mottaker"));

        dokdistKlient.distribuerJournalpost(journalpostId, mottaker);
    }

    private static JournalpostId finnJournalpostId(ProsessTaskData prosessTaskData) {
        String journalpostId = prosessTaskData.getPropertyValue("journalpostId");
        return new JournalpostId(journalpostId);
    }


}
