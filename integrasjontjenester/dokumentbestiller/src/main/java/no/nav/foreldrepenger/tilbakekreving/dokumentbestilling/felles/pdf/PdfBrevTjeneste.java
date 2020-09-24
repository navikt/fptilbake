package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist.DokdistKlient;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header.TekstformatererHeader;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;
import no.nav.foreldrepenger.tilbakekreving.pdfgen.PdfGenerator;

@ApplicationScoped
public class PdfBrevTjeneste {
    private JournalføringTjeneste journalføringTjeneste;
    private DokdistKlient dokdistKlient;
    private PdfGenerator pdfGenerator = new PdfGenerator();

    public PdfBrevTjeneste() {
        // for CDI proxy
    }

    @Inject
    public PdfBrevTjeneste(JournalføringTjeneste journalføringTjeneste, DokdistKlient dokdistKlient) {
        this.journalføringTjeneste = journalføringTjeneste;
        this.dokdistKlient = dokdistKlient;
    }

    public byte[] genererForhåndsvisning(BrevData data) {
        String html = lagHtml(data);
        return pdfGenerator.genererPDFMedLogo(html);
    }

    public JournalpostIdOgDokumentId sendBrev(Long behandlingId, BrevData data) {
        String html = lagHtml(data);
        byte[] pdf = pdfGenerator.genererPDFMedLogo(html);
        JournalpostIdOgDokumentId dokumentreferanse = journalføringTjeneste.journalførUtgåendeVedtaksbrev(behandlingId, data.getMetadata(), data.getMottaker(), pdf);

        //TODO bør gjøre distribuering i egen prosesstask for å unngå å jounalføre flere ganger hvis distribuering feiler
        dokdistKlient.distribuerJournalpost(dokumentreferanse.getJournalpostId(), data.getMottaker(), data.getMetadata().getMottakerAdresse());

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
