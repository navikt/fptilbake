package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header.TekstformatererHeader;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.pdfgen.PdfGenerator;
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

    public JournalpostIdOgDokumentId sendBrev(Long behandlingId, BrevData data) {
        String html = lagHtml(data);
        byte[] pdf = pdfGenerator.genererPDFMedLogo(html);
        JournalpostIdOgDokumentId dokumentreferanse = journalføringTjeneste.journalførUtgåendeVedtaksbrev(behandlingId, data.getMetadata(), data.getMottaker(), pdf);

        ProsessTaskData prosessTaskData = new ProsessTaskData(PubliserJournalpostTask.TASKTYPE);
        prosessTaskData.setProperty("behandlingId", behandlingId.toString());
        prosessTaskData.setProperty("journalpostId", dokumentreferanse.getJournalpostId().getVerdi());
        prosessTaskData.setProperty("mottaker", data.getMottaker().name());
        if (data.getMottaker() != BrevMottaker.BRUKER) {
            Adresseinfo mottakerAdresse = data.getMetadata().getMottakerAdresse();
            prosessTaskData.setProperty("mottaker.adresselinje1", mottakerAdresse.getAdresselinje1());
            prosessTaskData.setProperty("mottaker.adresselinje2", mottakerAdresse.getAdresselinje2());
            prosessTaskData.setProperty("mottaker.adresselinje3", mottakerAdresse.getAdresselinje3());
            prosessTaskData.setProperty("mottaker.postnr", mottakerAdresse.getPostNr());
            prosessTaskData.setProperty("mottaker.poststed", mottakerAdresse.getPoststed());
            prosessTaskData.setProperty("mottaker.land", mottakerAdresse.getLand());
            if (mottakerAdresse.getAdresselinje4() != null) {
                throw new IllegalArgumentException("adresselinje4 er ikke støttet av dokdist");
            }
        }
        prosessTaskRepository.lagre(prosessTaskData);

        return dokumentreferanse;
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
