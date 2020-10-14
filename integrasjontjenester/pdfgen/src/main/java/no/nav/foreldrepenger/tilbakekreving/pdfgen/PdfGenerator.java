package no.nav.foreldrepenger.tilbakekreving.pdfgen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.openhtmltopdf.util.XRLog;

import no.nav.foreldrepenger.tilbakekreving.pdfgen.validering.PdfaValidator;

public class PdfGenerator {

    private static final Map<String, byte[]> FONT_CACHE = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(PdfGenerator.class);

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

        long t0 = System.currentTimeMillis();
        genererPDF(html, baos, dokumentVariant);
        byte[] bytes = baos.toByteArray();

        //Midlertidig kode, kan fjernes i desember 2020. appdynamics kan brukes for å oppdage trege tilfeller
        logger.info("Produserte PDF fra html {} kB tok {} ms", bytes.length / 1024, System.currentTimeMillis() - t0);

        if (dokumentVariant == DokumentVariant.ENDELIG) {
            //validering er for treig for å brukes for interaktiv bruk, tar typisk 1-2 sekunder pr dokument
            //validering er også bare nødvendig før journalføring, så det er OK
            t0 = System.currentTimeMillis();
            PdfaValidator.validatePdf(bytes);

            //Midlertidig kode, kan fjernes i desember 2020. logger tidsforbruk for å vurdere om validering også skal gjøres på forhåndsvisning
            logger.info("Validerte PDF/A {} kB tok {} ms", bytes.length / 1024, System.currentTimeMillis() - t0);
        }

        return bytes;
    }

    private void genererPDF(String htmlContent, ByteArrayOutputStream outputStream, DokumentVariant dokumentVariant) {
        String htmlDocument = appendHtmlMetadata(htmlContent, DocFormat.PDF, dokumentVariant);

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

    private String appendHtmlMetadata(String html, DocFormat format, DokumentVariant dokumentVariant) {
        StringBuilder builder = new StringBuilder();
        //nødvendig doctype for å støtte non-breaking space i openhtmltopdf
        builder.append("<!DOCTYPE html PUBLIC");
        builder.append(" \"-//OPENHTMLTOPDF//DOC XHTML Character Entities Only 1.0//EN\" \"\">");

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
