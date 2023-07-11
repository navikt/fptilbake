package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist.Distribusjonstype;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist.DokdistKlient;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("publiser.journalpost.dokdist")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class PubliserJournalpostTask implements ProsessTaskHandler {

    protected static final String MOTTAKER = "mottaker";
    protected static final String DISTRIBUSJONSTYPE = "distribusjonstype";
    protected static final String JOURNALPOST_ID = "journalpostId";
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
        var journalpostId = new JournalpostId(prosessTaskData.getPropertyValue(JOURNALPOST_ID));
        var mottaker = BrevMottaker.valueOf(prosessTaskData.getPropertyValue(MOTTAKER));
        var distribusjonstype = prosessTaskData.getPropertyValue(DISTRIBUSJONSTYPE);
        dokdistKlient.distribuerJournalpost(journalpostId, mottaker, Optional.ofNullable(distribusjonstype).map(Distribusjonstype::valueOf).orElse(null));
    }
}
