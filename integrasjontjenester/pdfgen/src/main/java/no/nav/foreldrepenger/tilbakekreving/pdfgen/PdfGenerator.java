package no.nav.foreldrepenger.tilbakekreving.pdfgen;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

    private String appendHtmlMetadata(String html, DocFormat format) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        builder.append("<head>");
        builder.append("<meta charset=\"UTF-8\" />");
        builder.append("<style>");
        builder.append(hentCss(format));
        builder.append("</style>");
        builder.append("</head>");
        builder.append("<body>");
        builder.append("<div id=\"content\">");
        builder.append(html);
        builder.append("</div>");
        builder.append("</body>");
        builder.append("</html>");
        String s = builder.toString();
        return s;
    }

    public byte[] genererPDF(String html) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        genererPDF(html, baos);
        return baos.toByteArray();
    }

    public void genererPDF(String htmlContent, ByteArrayOutputStream outputStream) {
        String htmlDocument = appendHtmlMetadata(htmlContent, DocFormat.PDF);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        try {
            builder
                .useFont(FileStructureUtil.getFont("SourceSansPro-Regular.ttf"), "Source Sans Pro", 400, BaseRendererBuilder.FontStyle.NORMAL, true)
                .useFont(FileStructureUtil.getFont("SourceSansPro-Bold.ttf"), "Source Sans Pro", 700, BaseRendererBuilder.FontStyle.OBLIQUE, true)
                .useFont(FileStructureUtil.getFont("SourceSansPro-It.ttf"), "Source Sans Pro", 400, BaseRendererBuilder.FontStyle.ITALIC, true)
                .useColorProfile(FileStructureUtil.getColorProfile())
                .useSVGDrawer(new BatikSVGDrawer())
                .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
                .withHtmlContent(htmlDocument, "")
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
