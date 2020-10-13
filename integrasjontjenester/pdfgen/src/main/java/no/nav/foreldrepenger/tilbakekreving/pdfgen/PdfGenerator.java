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
import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.openhtmltopdf.util.XRLog;

import no.nav.foreldrepenger.tilbakekreving.pdfgen.validering.PdfaValidator;

public class PdfGenerator {

    private static final Map<String, byte[]> FONT_CACHE = new HashMap<>();

    static {
        XRLog.setLoggingEnabled(true);
        XRLog.setLoggerImpl(new Slf4jLogger());
    }

    public byte[] genererPDFMedLogo(String html, DokumentVariant dokumentVariant) {
        String logo = FileStructureUtil.readResourceAsString("pdf/nav_logo_svg.html");
        return genererPDF(logo + html, dokumentVariant);
    }

    public byte[] genererPDF(String html, DokumentVariant dokumentVariant) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        genererPDF(html, baos, dokumentVariant);
        byte[] bytes = baos.toByteArray();

        if (dokumentVariant == DokumentVariant.ENDELIG) {
            //validering er for treig for å brukes for interaktiv bruk, tar typisk 1-2 sekunder pr dokument
            //validering er også bare nødvendig før journalføring, så det er OK
            PdfaValidator.validatePdf(bytes);
        }

        return bytes;
    }

    private void genererPDF(String htmlContent, ByteArrayOutputStream outputStream, DokumentVariant dokumentVariant) {
        String htmlDocument = appendHtmlMetadata(htmlContent, DocFormat.PDF, dokumentVariant);

        //trenger komplett normalfont for å passere PDF/A-validering, inntil pdfbox/openhtmltopdf forbedrer subset-funksjonalitet
        //bruker subset for forhåndsvisning for å få raskere respons
        boolean inkluderKomplettNormalfont = dokumentVariant == DokumentVariant.ENDELIG;

        PdfRendererBuilder builder = new PdfRendererBuilder();
        try {
            builder
                .useFont(fontSupplier("SourceSansPro-Regular.ttf"), "Source Sans Pro", 400, BaseRendererBuilder.FontStyle.NORMAL, !inkluderKomplettNormalfont)
                .useFont(fontSupplier("SourceSansPro-Bold.ttf"), "Source Sans Pro", 700, BaseRendererBuilder.FontStyle.OBLIQUE, true)
                .useFont(fontSupplier("SourceSansPro-It.ttf"), "Source Sans Pro", 400, BaseRendererBuilder.FontStyle.ITALIC, true)
                .useColorProfile(FileStructureUtil.getColorProfile())
                .useSVGDrawer(new BatikSVGDrawer())
                .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_A)
                .withHtmlContent(htmlDocument, "")
                .toStream(outputStream)
                .useFastMode()
                .buildPdfRenderer()
                .createPDF();
        } catch (IOException e) {
            throw new RuntimeException("Feil ved generering av pdf", e);
        }
    }

    private String appendHtmlMetadata(String html, DocFormat format, DokumentVariant dokumentVariant) {
        StringBuilder builder = new StringBuilder();
        //nødvendig doctype for å støtte non-breaking space i openhtmltopdf
        //builder.append("<!DOCTYPE html PUBLIC");
        //builder.append(" \"-//OPENHTMLTOPDF//DOC XHTML Character Entities Only 1.0//EN\" \"\">");

        builder.append("<html>");
        builder.append("<head>");
        builder.append("<meta charset=\"UTF-8\" />");
        builder.append("<style>");
        builder.append(hentCss(format));
        builder.append("</style>");
        builder.append("</head>");
        builder.append(lagBodyStartTag(dokumentVariant));
        builder.append("<div id=\"content\">");
        builder.append(html);
        builder.append("</div>");
        builder.append("</body>");
        builder.append("</html>");
        return builder.toString();
    }

    private String lagBodyStartTag(DokumentVariant dokumentVariant) {
        switch (dokumentVariant) {
            case ENDELIG:
                return "<body>";
            case UTKAST:
                return "<body class=\"utkast\">";
            default:
                throw new IllegalArgumentException("Ikke-støttet dokumentvariant: " + dokumentVariant);
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
