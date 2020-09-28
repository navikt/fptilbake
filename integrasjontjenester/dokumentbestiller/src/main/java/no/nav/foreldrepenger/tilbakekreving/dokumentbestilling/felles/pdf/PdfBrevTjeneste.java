package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
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

    public JournalpostIdOgDokumentId sendVedtaksbrev(Long behandlingId, BrevData data) {
        return sendBrev(behandlingId, data, Dokumentkategori.Vedtaksbrev);
    }

    public JournalpostIdOgDokumentId sendBrevSomIkkeErVedtaksbrev(Long behandlingId, BrevData data) {
        return sendBrev(behandlingId, data, Dokumentkategori.Brev);
    }

    private JournalpostIdOgDokumentId sendBrev(Long behandlingId, BrevData data, Dokumentkategori dokumentkategori) {
        String html = lagHtml(data);
        byte[] pdf = pdfGenerator.genererPDFMedLogo(html);
        JournalpostIdOgDokumentId dokumentreferanse = journalføringTjeneste.journalførUtgåendeBrev(behandlingId, dokumentkategori, data.getMetadata(), data.getMottaker(), pdf);

        ProsessTaskData prosessTaskData = new ProsessTaskData(PubliserJournalpostTask.TASKTYPE);
        prosessTaskData.setProperty("behandlingId", behandlingId.toString());
        prosessTaskData.setProperty("journalpostId", dokumentreferanse.getJournalpostId().getVerdi());
        prosessTaskData.setProperty("mottaker", data.getMottaker().name());
        if (data.getMottaker() != BrevMottaker.BRUKER) {
            Adresseinfo mottakerAdresse = data.getMetadata().getMottakerAdresse();
            setHvisHarVerdi(prosessTaskData, "mottaker.adresselinje1", mottakerAdresse.getAdresselinje1());
            setHvisHarVerdi(prosessTaskData, "mottaker.adresselinje2", mottakerAdresse.getAdresselinje2());
            setHvisHarVerdi(prosessTaskData, "mottaker.adresselinje3", mottakerAdresse.getAdresselinje3());
            setHvisHarVerdi(prosessTaskData, "mottaker.postnr", mottakerAdresse.getPostNr());
            setHvisHarVerdi(prosessTaskData, "mottaker.poststed", mottakerAdresse.getPoststed());
            setHvisHarVerdi(prosessTaskData, "mottaker.land", mottakerAdresse.getLand());
            if (mottakerAdresse.getAdresselinje4() != null && !mottakerAdresse.getAdresselinje4().isBlank()) {
                throw new IllegalArgumentException("adresselinje4 er ikke støttet av dokdist");
            }
        }
        prosessTaskRepository.lagre(prosessTaskData);

        return dokumentreferanse;
    }

    private static void setHvisHarVerdi(ProsessTaskData data, String navn, String verdi) {
        if (verdi != null && !verdi.isBlank()) {
            data.setProperty(navn, verdi);
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
