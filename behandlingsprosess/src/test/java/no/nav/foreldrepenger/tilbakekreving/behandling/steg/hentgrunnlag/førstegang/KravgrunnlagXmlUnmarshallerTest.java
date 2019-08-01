package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class KravgrunnlagXmlUnmarshallerTest {

    @Test
    public void skal_unmarshalle() {
        String xml = getInputXML("xml/kravgrunnlag_detaljert.xml");
        KravgrunnlagXmlUnmarshaller.unmarshall(0L, xml);
    }

    private String getInputXML(String filename) {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError("Feil i testoppsett", e);
        }
    }
}
