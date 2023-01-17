package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.migrasjon;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.codahale.metrics.annotation.Timed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.FagsystemId;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/migrasjon")
@ApplicationScoped
@Transactional
public class MigrasjonRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(MigrasjonRestTjeneste.class);

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
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
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
                    String saksnummer = FagsystemId.parse(fagsystemId).getSaksnummer().getVerdi();
                    økonomiMottattXmlRepository.oppdaterSaksnummer(økonomiXmlMottatt.getId(), saksnummer);
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    logger.warn("kan ikke prossesere XML med Id={}.Fikk følgende problemer={}", økonomiXmlMottatt.getId(), e.getMessage());
                }
            }
        }
        return Response.status(Response.Status.OK).build();
    }


}
