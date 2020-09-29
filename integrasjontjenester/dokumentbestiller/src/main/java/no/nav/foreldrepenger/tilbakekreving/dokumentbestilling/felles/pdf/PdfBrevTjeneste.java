package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header.TekstformatererHeader;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.pdfgen.PdfGenerator;
import no.nav.journalpostapi.dto.dokument.Dokumentkategori;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class PdfBrevTjeneste {
    private JournalføringTjeneste journalføringTjeneste;

    private PdfGenerator pdfGenerator = new PdfGenerator();
    private ProsessTaskRepository prosessTaskRepository;

    public PdfBrevTjeneste() {
        // for CDI proxy
    }

    @Inject
    public PdfBrevTjeneste(JournalføringTjeneste journalføringTjeneste, ProsessTaskRepository prosessTaskRepository) {
        this.journalføringTjeneste = journalføringTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    public byte[] genererForhåndsvisning(BrevData data) {
        String html = lagHtml(data);
        return pdfGenerator.genererPDFMedLogo(html);
    }

    public JournalpostIdOgDokumentId sendBrev(Long behandlingId, DetaljertBrevType detaljertBrevType, BrevData data) {
        return sendBrev(behandlingId, detaljertBrevType, null, data);
    }

    public JournalpostIdOgDokumentId sendBrev(Long behandlingId, DetaljertBrevType detaljertBrevType, Long varsletBeløp, BrevData data) {
        valider(detaljertBrevType, varsletBeløp);

        String html = lagHtml(data);
        byte[] pdf = pdfGenerator.genererPDFMedLogo(html);
        JournalpostIdOgDokumentId dokumentreferanse = journalføringTjeneste.journalførUtgåendeBrev(behandlingId, mapBrevTypeTilDokumentKategori(detaljertBrevType), data.getMetadata(), data.getMottaker(), pdf);

        ProsessTaskData prosessTaskData = new ProsessTaskData(PubliserJournalpostTask.TASKTYPE);
        prosessTaskData.setProperty("behandlingId", behandlingId.toString());
        prosessTaskData.setProperty("journalpostId", dokumentreferanse.getJournalpostId().getVerdi());
        prosessTaskData.setProperty("mottaker", data.getMottaker().name());
        prosessTaskData.setProperty("detaljertBrevType", detaljertBrevType.name());
        if (varsletBeløp != null) {
            //TODO helst lag en løsning hvor varsletBeløp ikke er i generell kode for brev
            prosessTaskData.setProperty("varsletBeloep", Long.toString(varsletBeløp));
        }
        prosessTaskRepository.lagre(prosessTaskData);

        return dokumentreferanse;
    }

    private static void valider(DetaljertBrevType brevType, Long varsletBeløp) {
        boolean erVarsel = brevType == DetaljertBrevType.VARSEL || brevType == DetaljertBrevType.KORRIGERT_VARSEL;
        boolean harVarsletBeløp = varsletBeløp != null;
        if (erVarsel != harVarsletBeløp) {
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
