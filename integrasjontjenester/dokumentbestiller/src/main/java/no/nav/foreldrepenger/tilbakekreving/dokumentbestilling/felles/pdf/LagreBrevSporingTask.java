package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("brev.sporing")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class LagreBrevSporingTask implements ProsessTaskHandler {

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
        String tittel = base64Decode(prosessTaskData.getPropertyValue("tittel"));

        historikkinnslagBrevTjeneste.opprettHistorikkinnslagBrevSendt(behandlingId, dokumentreferanse, brevType, mottaker, tittel);
        brevSporingTjeneste.lagreInfoOmUtsendtBrev(behandlingId, dokumentreferanse, brevType);
    }

    private String base64Decode(String tittel) {
        if (tittel == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(tittel.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
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
