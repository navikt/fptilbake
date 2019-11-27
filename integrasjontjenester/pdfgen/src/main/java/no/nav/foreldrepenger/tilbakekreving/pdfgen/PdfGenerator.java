package no.nav.foreldrepenger.tilbakekreving.pdfgen;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;

public class PdfGenerator {

    public static void main(String[] args) throws Exception {
        PdfGenerator tjeneste = new PdfGenerator();

        String html = FileStructureUtil.readResourceAsString("test/in.html");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tjeneste.genererPDF(html, baos);

        try (FileOutputStream os = new FileOutputStream("ouput.pdf")) {
            baos.writeTo(os);
        }
    }

    private Document appendHtmlMetadata(String html, DocFormat format) {
        Document document = Jsoup.parse(("<div id=\"content\">" + html + "</div>"));
        Element head = document.head();

        head.append("<meta charset=\"UTF-8\">");
        head.append("<style>" + hentCss(format) + "</style>");

        return document;
    }

    public byte[] genererPDF(String html) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        genererPDF(html, baos);
        return baos.toByteArray();
    }

    public void genererPDF(String html, ByteArrayOutputStream outputStream) {
        Document document = appendHtmlMetadata(html, DocFormat.PDF);
        org.w3c.dom.Document doc = new W3CDom().fromJsoup(document);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        try {
            builder
                .useFont(FileStructureUtil.getFont("SourceSansPro-Regular.ttf"), "Source Sans Pro", 400, BaseRendererBuilder.FontStyle.NORMAL, true)
                .useFont(FileStructureUtil.getFont("SourceSansPro-Bold.ttf"), "Source Sans Pro", 700, BaseRendererBuilder.FontStyle.OBLIQUE, true)
                .useFont(FileStructureUtil.getFont("SourceSansPro-It.ttf"), "Source Sans Pro", 400, BaseRendererBuilder.FontStyle.ITALIC, true)
                .useColorProfile(FileStructureUtil.getColorProfile())
                .useSVGDrawer(new BatikSVGDrawer())
                .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
                .withW3cDocument(doc, "")
                .toStream(outputStream)
                .useFastMode()
                .buildPdfRenderer()
                .createPDF();
        } catch (IOException e) {
            throw new RuntimeException("Feil ved generering av pdf", e);
        }
    }

    private String hentCss(DocFormat format) {
        return FileStructureUtil.readResourceAsString("formats/" + format.name().toLowerCase() + "/style.css");
    }

}
