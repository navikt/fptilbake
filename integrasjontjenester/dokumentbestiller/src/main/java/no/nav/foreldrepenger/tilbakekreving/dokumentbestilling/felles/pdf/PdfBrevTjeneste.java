package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskBehandlingUtil;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header.TekstformatererHeader;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.pdfgen.DokumentVariant;
import no.nav.foreldrepenger.tilbakekreving.pdfgen.PdfGenerator;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.SelvbetjeningTilbakekrevingStøtte;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendBeskjedUtsendtVarselTilSelvbetjeningTask;
import no.nav.journalpostapi.dto.dokument.Dokumentkategori;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class PdfBrevTjeneste {

    private Logger logger = LoggerFactory.getLogger(PdfBrevTjeneste.class);

    private JournalføringTjeneste journalføringTjeneste;

    private PdfGenerator pdfGenerator = new PdfGenerator();
    private BehandlingRepository behandlingRepository;
    private ProsessTaskTjeneste taskTjeneste;

    public PdfBrevTjeneste() {
        // for CDI proxy
    }

    @Inject
    public PdfBrevTjeneste(JournalføringTjeneste journalføringTjeneste, BehandlingRepository behandlingRepository, ProsessTaskTjeneste taskTjeneste) {
        this.journalføringTjeneste = journalføringTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.taskTjeneste = taskTjeneste;
    }

    public byte[] genererForhåndsvisning(BrevData data) {
        String html = lagHtml(data);
        return pdfGenerator.genererPDFMedLogo(html, DokumentVariant.UTKAST);
    }

    public void sendBrev(Long behandlingId, DetaljertBrevType detaljertBrevType, BrevData data) {
        sendBrev(behandlingId, detaljertBrevType, null, null, data);
    }

    public void sendBrev(Long behandlingId, DetaljertBrevType detaljertBrevType, Long varsletBeløp, String fritekst, BrevData data) {
        valider(detaljertBrevType, varsletBeløp);
        valider(detaljertBrevType, data);

        JournalpostIdOgDokumentId dokumentreferanse = lagOgJournalførBrev(behandlingId, detaljertBrevType, data);
        lagTaskerForUtsendingOgSporing(behandlingId, detaljertBrevType, varsletBeløp, fritekst, data, dokumentreferanse);
    }

    private void lagTaskerForUtsendingOgSporing(Long behandlingId, DetaljertBrevType detaljertBrevType, Long varsletBeløp, String fritekst, BrevData data, JournalpostIdOgDokumentId dokumentreferanse) {
        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        taskGruppe.addNesteSekvensiell(lagPubliserJournalpostTask(behandling, data, dokumentreferanse, detaljertBrevType.getBrevType()));
        taskGruppe.addNesteSekvensiell(lagSporingBrevTask(behandling, detaljertBrevType, data, dokumentreferanse));
        if (detaljertBrevType.gjelderVarsel() && data.getMottaker() == BrevMottaker.BRUKER) {
            taskGruppe.addNesteSekvensiell(lagSporingVarselBrevTask(behandling, varsletBeløp, fritekst));
            lagSendBeskjedTilSelvbetjeningTask(behandling).ifPresent(taskGruppe::addNesteSekvensiell);
        }
        taskTjeneste.lagre(taskGruppe);
    }

    private JournalpostIdOgDokumentId lagOgJournalførBrev(Long behandlingId, DetaljertBrevType detaljertBrevType, BrevData data) {
        String html = lagHtml(data);
        byte[] pdf = pdfGenerator.genererPDFMedLogo(html, DokumentVariant.ENDELIG);
        return journalføringTjeneste.journalførUtgåendeBrev(behandlingId, mapBrevTypeTilDokumentKategori(detaljertBrevType), data.getMetadata(), data.getMottaker(), pdf);
    }

    private ProsessTaskData lagPubliserJournalpostTask(Behandling behandling, BrevData brevdata, JournalpostIdOgDokumentId dokumentreferanse, BrevType brevType) {
        ProsessTaskData data = ProsessTaskData.forProsessTask(PubliserJournalpostTask.class);
        ProsessTaskBehandlingUtil.setBehandling(data, behandling);
        data.setProperty(PubliserJournalpostTask.JOURNALPOST_ID, dokumentreferanse.getJournalpostId().getVerdi());
        data.setProperty(PubliserJournalpostTask.MOTTAKER, brevdata.getMottaker().name());
        data.setProperty(PubliserJournalpostTask.DISTRIBUSJONSTYPE, DistribusjonstypeUtleder.utledFor(brevType).name());
        return data;
    }

    private ProsessTaskData lagSporingBrevTask(Behandling behandling, DetaljertBrevType detaljertBrevType, BrevData brevdata, JournalpostIdOgDokumentId dokumentreferanse) {
        ProsessTaskData data = ProsessTaskData.forProsessTask(LagreBrevSporingTask.class);
        ProsessTaskBehandlingUtil.setBehandling(data, behandling);
        data.setProperty(LagreBrevSporingTask.JOURNALPOST_ID, dokumentreferanse.getJournalpostId().getVerdi());
        data.setProperty(LagreBrevSporingTask.DOKUMENT_ID, dokumentreferanse.getDokumentId());
        data.setProperty(LagreBrevSporingTask.MOTTAKER, brevdata.getMottaker().name());
        data.setProperty(LagreBrevSporingTask.DETALJERT_BREV_TYPE, detaljertBrevType.name());
        if (brevdata.getTittel() != null) {
            data.setProperty(LagreBrevSporingTask.TITTEL, Base64.getEncoder().encodeToString(brevdata.getTittel().getBytes(StandardCharsets.UTF_8)));
        }
        return data;
    }

    private ProsessTaskData lagSporingVarselBrevTask(Behandling behandling, Long varsletBeløp, String fritekst) {
        ProsessTaskData data = ProsessTaskData.forProsessTask(LagreVarselBrevSporingTask.class);
        ProsessTaskBehandlingUtil.setBehandling(data, behandling);
        data.setProperty(LagreVarselBrevSporingTask.VARSLET_BELOEP, Long.toString(varsletBeløp));
        data.setPayload(fritekst);
        return data;
    }

    private Optional<ProsessTaskData> lagSendBeskjedTilSelvbetjeningTask(Behandling behandling) {
        if (SelvbetjeningTilbakekrevingStøtte.harStøtteFor(behandling)) {
            ProsessTaskData data = ProsessTaskData.forProsessTask(SendBeskjedUtsendtVarselTilSelvbetjeningTask.class);
            ProsessTaskBehandlingUtil.setBehandling(data, behandling);
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

    private static void valider(DetaljertBrevType brevType, BrevData data) {
        if (brevType == DetaljertBrevType.FRITEKST && data.getTittel() == null) {
            throw new IllegalArgumentException("Utvikler-feil: For brevType = " + brevType + " må tittel være satt");
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
