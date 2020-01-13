package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.migrasjon;

import com.codahale.metrics.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningTekniskRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/migrasjon")
@ApplicationScoped
@Transaction
public class MigrasjonRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(ForvaltningTekniskRestTjeneste.class);

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;

    public MigrasjonRestTjeneste() {
        // for CDI
    }

    @Inject
    public MigrasjonRestTjeneste(ØkonomiMottattXmlRepository økonomiMottattXmlRepository) {
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
    }

    @POST
    @Timed
    @Path("/okoxmlmottatt")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(tags = "migrasjon", description = "Tjeneste for å migrere saksnummer i OKO_XML_MOTTATT",
        responses = {
            @ApiResponse(responseCode = "200", description = "Migrasjon er ferdig"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.DRIFT)
    public Response migrereSaksnummerIOkoXmlMottatt() {
        List<ØkonomiXmlMottatt> xmlMeldinger = økonomiMottattXmlRepository.hentAlleMeldingerUtenSaksnummer();
        if (!xmlMeldinger.isEmpty()) {
            for (ØkonomiXmlMottatt økonomiXmlMottatt : xmlMeldinger) {
                String melding = økonomiXmlMottatt.getMottattXml();
                DocumentBuilder documentBuilder = null;
                try {
                    documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document document = documentBuilder.parse(new InputSource(new StringReader(melding)));
                    String fagsystemId = document.getElementsByTagName("urn:fagsystemId").item(0).getTextContent();
                    String saksnummer = finnSaksnummer(fagsystemId);
                    økonomiMottattXmlRepository.oppdaterSaksnummer(økonomiXmlMottatt.getId(), saksnummer);
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    logger.warn("kan ikke prossesere XML med Id={}.Fikk følgende problemer={}", økonomiXmlMottatt.getId(), e.getMessage());
                }
            }
        }
        return Response.status(Response.Status.OK).build();
    }

    private String finnSaksnummer(String fagsystemId) {
        return fagsystemId.substring(0, fagsystemId.length() - 3);
    }
}
