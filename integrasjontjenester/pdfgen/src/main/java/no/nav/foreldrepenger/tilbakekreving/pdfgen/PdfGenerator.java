package no.nav.foreldrepenger.tilbakekreving.pdfgen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;

public class PdfGenerator {

    private static final Map<String, byte[]> FONT_CACHE = new HashMap<>();

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
        return builder.toString();
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
                .useFont(fontSupplier("SourceSansPro-Regular.ttf"), "Source Sans Pro", 400, BaseRendererBuilder.FontStyle.NORMAL, true)
                .useFont(fontSupplier("SourceSansPro-Bold.ttf"), "Source Sans Pro", 700, BaseRendererBuilder.FontStyle.OBLIQUE, true)
                .useFont(fontSupplier("SourceSansPro-It.ttf"), "Source Sans Pro", 400, BaseRendererBuilder.FontStyle.ITALIC, true)
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

    private FSSupplier<InputStream> fontSupplier(String fontName) {
        if (FONT_CACHE.containsKey(fontName)) {
            byte[] bytes = FONT_CACHE.get(fontName);
            return () -> new ByteArrayInputStream(bytes);
        }
        byte[] bytes = FileStructureUtil.readResource("fonts/" + fontName);
        FONT_CACHE.put(fontName, bytes);
        return () -> new ByteArrayInputStream(bytes);
    }

    private String hentCss(DocFormat format) {
        return FileStructureUtil.readResourceAsString("formats/" + format.name().toLowerCase() + "/style.css");
    }

}
