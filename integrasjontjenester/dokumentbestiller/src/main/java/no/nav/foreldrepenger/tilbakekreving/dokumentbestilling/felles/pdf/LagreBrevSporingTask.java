package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

    protected static final String MOTTAKER = "mottaker";
    protected static final String DOKUMENT_ID = "dokumentId";
    protected static final String JOURNALPOST_ID = "journalpostId";
    protected static final String DETALJERT_BREV_TYPE = "detaljertBrevType";
    protected static final String TITTEL = "tittel";
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
        var behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        var mottaker = BrevMottaker.valueOf(prosessTaskData.getPropertyValue(MOTTAKER));
        var dokumentId = prosessTaskData.getPropertyValue(DOKUMENT_ID);
        var journalpostId = prosessTaskData.getPropertyValue(JOURNALPOST_ID);
        var dokumentreferanse = new JournalpostIdOgDokumentId(new JournalpostId(journalpostId), dokumentId);
        var brevType = DetaljertBrevType.valueOf(prosessTaskData.getPropertyValue(DETALJERT_BREV_TYPE));
        var tittel = base64Decode(prosessTaskData.getPropertyValue(TITTEL));

        historikkinnslagBrevTjeneste.opprettHistorikkinnslagBrevSendt(behandlingId, dokumentreferanse, brevType, mottaker, tittel);
        brevSporingTjeneste.lagreInfoOmUtsendtBrev(behandlingId, dokumentreferanse, brevType);
    }

    private String base64Decode(String tittel) {
        if (tittel == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(tittel.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
