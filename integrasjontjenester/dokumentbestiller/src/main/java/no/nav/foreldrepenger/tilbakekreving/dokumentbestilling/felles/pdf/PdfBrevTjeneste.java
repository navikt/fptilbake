package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header.TekstformatererHeader;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.pdfgen.PdfGenerator;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendBeskjedUtsendtVarselTilSelvbetjeningTask;
import no.nav.journalpostapi.dto.dokument.Dokumentkategori;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class PdfBrevTjeneste {

    private Logger logger = LoggerFactory.getLogger(PdfBrevTjeneste.class);

    private JournalføringTjeneste journalføringTjeneste;

    private PdfGenerator pdfGenerator = new PdfGenerator();
    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;

    public PdfBrevTjeneste() {
        // for CDI proxy
    }

    @Inject
    public PdfBrevTjeneste(JournalføringTjeneste journalføringTjeneste, BehandlingRepository behandlingRepository, ProsessTaskRepository prosessTaskRepository) {
        this.journalføringTjeneste = journalføringTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    public byte[] genererForhåndsvisning(BrevData data) {
        String html = lagHtml(data);
        return pdfGenerator.genererPDFMedLogo(html);
    }

    public JournalpostIdOgDokumentId sendBrev(Long behandlingId, DetaljertBrevType detaljertBrevType, BrevData data) {
        return sendBrev(behandlingId, detaljertBrevType, null, null, data);
    }

    public JournalpostIdOgDokumentId sendBrev(Long behandlingId, DetaljertBrevType detaljertBrevType, Long varsletBeløp, String fritekst, BrevData data) {
        JournalpostIdOgDokumentId dokumentreferanse = lagOgJournalførBrev(behandlingId, detaljertBrevType, varsletBeløp, data);

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        taskGruppe.addNesteSekvensiell(lagPubliserJournalpostTask(behandlingId, data, dokumentreferanse));
        taskGruppe.addNesteSekvensiell(lagSporingBrevTask(behandlingId, detaljertBrevType, data, dokumentreferanse));
        if (detaljertBrevType.gjelderVarsel()) {
            taskGruppe.addNesteSekvensiell(lagSporingVarselBrevTask(behandlingId, varsletBeløp, fritekst));
            lagSendBeskjedTilSelvbetjeningTask(behandlingId).ifPresent(taskGruppe::addNesteSekvensiell);
        }
        prosessTaskRepository.lagre(taskGruppe);
        return dokumentreferanse;
    }

    private JournalpostIdOgDokumentId lagOgJournalførBrev(Long behandlingId, DetaljertBrevType detaljertBrevType, Long varsletBeløp, BrevData data) {
        valider(detaljertBrevType, varsletBeløp);

        String html = lagHtml(data);
        byte[] pdf = pdfGenerator.genererPDFMedLogo(html);
        return journalføringTjeneste.journalførUtgåendeBrev(behandlingId, mapBrevTypeTilDokumentKategori(detaljertBrevType), data.getMetadata(), data.getMottaker(), pdf);
    }

    private ProsessTaskData lagPubliserJournalpostTask(Long behandlingId, BrevData brevdata, JournalpostIdOgDokumentId dokumentreferanse) {
        ProsessTaskData data = new ProsessTaskData(PubliserJournalpostTask.TASKTYPE);
        data.setProperty("behandlingId", behandlingId.toString());
        data.setProperty("journalpostId", dokumentreferanse.getJournalpostId().getVerdi());
        data.setProperty("mottaker", brevdata.getMottaker().name());
        return data;
    }

    private ProsessTaskData lagSporingBrevTask(Long behandlingId, DetaljertBrevType detaljertBrevType, BrevData brevdata, JournalpostIdOgDokumentId dokumentreferanse) {
        String taskType = LagreBrevSporingTask.TASKTYPE;
        ProsessTaskData data = new ProsessTaskData(taskType);
        data.setProperty("behandlingId", behandlingId.toString());
        data.setProperty("journalpostId", dokumentreferanse.getJournalpostId().getVerdi());
        data.setProperty("dokumentId", dokumentreferanse.getDokumentId());
        data.setProperty("mottaker", brevdata.getMottaker().name());
        data.setProperty("detaljertBrevType", detaljertBrevType.name());
        return data;
    }

    private ProsessTaskData lagSporingVarselBrevTask(Long behandlingId, Long varsletBeløp, String fritekst) {
        String taskType = LagreVarselBrevSporingTask.TASKTYPE;
        ProsessTaskData data = new ProsessTaskData(taskType);
        data.setProperty("behandlingId", behandlingId.toString());
        data.setProperty("varsletBeloep", Long.toString(varsletBeløp));
        data.setPayload(fritekst);
        return data;
    }

    private Optional<ProsessTaskData> lagSendBeskjedTilSelvbetjeningTask(Long behandlingId ){
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (SendBeskjedUtsendtVarselTilSelvbetjeningTask.kanSendeVarsel(behandling)) {
            ProsessTaskData data = new ProsessTaskData(SendBeskjedUtsendtVarselTilSelvbetjeningTask.TASKTYPE);
            data.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
            return Optional.of(data);
        } else {
            logger.info("Sender ikke beskjed til selvbetjening for varsel for behandlingId={} i sak={}", behandling.getId(), behandling.getFagsak().getSaksnummer().getVerdi());
            return Optional.empty();
        }
    }

    private static void valider(DetaljertBrevType brevType, Long varsletBeløp) {
        boolean harVarsletBeløp = varsletBeløp != null;
        if (brevType.gjelderVarsel() != harVarsletBeløp) {
            throw new IllegalArgumentException("Utvikler-feil: Varslet beløp skal brukes hvis, og bare hvis, brev gjelder varsel");
        }
    }

    private Dokumentkategori mapBrevTypeTilDokumentKategori(DetaljertBrevType brevType) {
        if (DetaljertBrevType.VEDTAK == brevType) {
            return Dokumentkategori.Vedtaksbrev;
        } else {
            return Dokumentkategori.Brev;
        }
    }

    private String lagHtml(BrevData data) {
        String header = lagHeader(data);
        String innholdHtml = lagInnhold(data);
        return header + innholdHtml + data.getVedleggHtml();
    }

    private String lagInnhold(BrevData data) {
        return DokprodTilHtml.dokprodInnholdTilHtml(data.getBrevtekst());
    }

    private String lagHeader(BrevData data) {
        return TekstformatererHeader.lagHeader(data.getMetadata(), data.getOverskrift());
    }
}
