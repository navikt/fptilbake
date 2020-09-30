package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import no.nav.foreldrepenger.tilbakekreving.pdfgen.DokumentVariant;
import no.nav.foreldrepenger.tilbakekreving.pdfgen.PdfGenerator;

public class VedtaksbrevVedleggTjeneste {

    private PdfGenerator pdfGenerator = new PdfGenerator();

    public byte[] lagVedlegg(VedtaksbrevData data, DokumentVariant dokumentVariant) {
        String dokumentSomSteng = TekstformatererVedtaksbrev.lagVedtaksbrevVedleggHtml(data.getVedtaksbrevData());
        return pdfGenerator.genererPDF(dokumentSomSteng, dokumentVariant);
    }
}
