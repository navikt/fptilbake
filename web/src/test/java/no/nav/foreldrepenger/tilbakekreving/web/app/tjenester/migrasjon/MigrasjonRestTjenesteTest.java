package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.migrasjon;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import javax.persistence.FlushModeType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrasjonRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository = new ØkonomiMottattXmlRepository(repositoryRule.getEntityManager());
    private MigrasjonRestTjeneste migrasjonRestTjeneste = new MigrasjonRestTjeneste(økonomiMottattXmlRepository);

    @Test
    public void migrer_saksnummer_i_oko_xml_mottatt() throws Exception {
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        økonomiMottattXmlRepository.lagreMottattXml(getInputXML("xml/kravgrunnlag_periode_YTEL.xml"));
        økonomiMottattXmlRepository.lagreMottattXml(getInputXML("xml/kravvedtakstatus_SPER.xml"));
        assertThat(økonomiMottattXmlRepository.hentAlleMeldingerUtenSaksnummer()).isNotEmpty();

        Response response = migrasjonRestTjeneste.migrereSaksnummerIOkoXmlMottatt();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(økonomiMottattXmlRepository.hentAlleMeldingerUtenSaksnummer()).isEmpty();
    }

    private String getInputXML(String filename) {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
            return Files.readString(path);
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError("Feil i testoppsett", e);
        }
    }
}
