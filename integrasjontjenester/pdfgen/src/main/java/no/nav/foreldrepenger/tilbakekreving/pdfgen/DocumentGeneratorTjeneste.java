package no.nav.foreldrepenger.tilbakekreving.pdfgen;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;

public class DocumentGeneratorTjeneste {

    public static void main(String[] args) throws Exception {
        DocumentGeneratorTjeneste tjeneste = new DocumentGeneratorTjeneste();

        String input = FileStructureUtil.readResourceAsString("test/markdown.in");
        Document document = tjeneste.appendHtmlMetadata(input, DocFormat.PDF);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tjeneste.genererPDF(document, baos);

        try (FileOutputStream os = new FileOutputStream("ouput.pdf")) {
            baos.writeTo(os);
        }
    }

    public Document appendHtmlMetadata(String markdown, DocFormat format) {
        String convertedTemplate = convertMarkdownTemplateToHtml(markdown);

        Document document = Jsoup.parse(("<div id=\"content\">" + convertedTemplate + "</div>"));
        Element head = document.head();

        head.append("<meta charset=\"UTF-8\">");
        head.append("<style>" + hentCss(format) + "</style>");

        return document;
    }

    public void genererPDF(Document html, ByteArrayOutputStream outputStream) {
        org.w3c.dom.Document doc = new W3CDom().fromJsoup(html);
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

    private String convertMarkdownTemplateToHtml(String content) {
        Node document = parseDocument(content);
        return renderToHTML(document);
    }

    private Node parseDocument(String content) {
        return getMarkdownToHtmlParser().parse(content);
    }

    private String renderToHTML(Node document) {
        return getHtmlRenderer().render(document);
    }

    private String hentCss(DocFormat format) {
        return FileStructureUtil.readResourceAsString("formats/" + format.name().toLowerCase() + "/style.css");
    }

    private List<Extension> getMarkdownExtensions() {
        return Arrays.asList(TablesExtension.create());
    }

    private Parser getMarkdownToHtmlParser() {
        return Parser.builder()
            .extensions(getMarkdownExtensions())
            .build();
    }

    private HtmlRenderer getHtmlRenderer() {
        return HtmlRenderer.builder()
            .extensions(getMarkdownExtensions())
            .build();
    }
}
