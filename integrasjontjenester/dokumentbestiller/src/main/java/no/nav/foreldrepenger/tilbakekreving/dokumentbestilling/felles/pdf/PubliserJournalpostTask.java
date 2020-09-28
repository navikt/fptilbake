package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist.Adresse;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist.AdresseType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist.DokdistKlient;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(PubliserJournalpostTask.TASKTYPE)
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
        String journalpostId = prosessTaskData.getPropertyValue("journalpostId");
        BrevMottaker mottaker = BrevMottaker.valueOf(prosessTaskData.getPropertyValue("mottaker"));

        switch (mottaker) {
            case BRUKER:
                dokdistKlient.distribuerJournalpostTilBruker(journalpostId);
                break;
            case VERGE:
                dokdistKlient.distribuerJournalpostTilVerge(journalpostId, lesMottakerAdresseinfo(prosessTaskData));
                break;
            default:
                throw new IllegalArgumentException("Ikke-støttet mottaker: " + mottaker);
        }
    }

    private Adresse lesMottakerAdresseinfo(ProsessTaskData prosessTaskData) {
        String land = prosessTaskData.getPropertyValue("mottaker.land");
        return Adresse.builder()
            .medAdresseType(land == null || "NO".equals(land) ? AdresseType.NORSK : AdresseType.UTENLANDSK)
            .medAdresselinje1(prosessTaskData.getPropertyValue("mottaker.adresselinje1"))
            .medAdresselinje2(prosessTaskData.getPropertyValue("mottaker.adresselinje2"))
            .medAdresselinje3(prosessTaskData.getPropertyValue("mottaker.adresselinje3"))
            .medPostnummer(prosessTaskData.getPropertyValue("mottaker.postnr"))
            .medPoststed(prosessTaskData.getPropertyValue("mottaker.poststed"))
            .medLand(land)
            .build();

    }

}
