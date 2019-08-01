package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.dokument;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.BestillDokumentTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndvisningVedtaksbrevPdfDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.SendVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.ForhåndvisningVedtaksbrevTekstDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

@Api(tags = "dokument")
@Path("/dokument")
@ApplicationScoped
public class DokumentRestTjeneste {

    private BestillDokumentTjeneste bestillDokumentTjeneste;

    @Inject
    public DokumentRestTjeneste(BestillDokumentTjeneste bestillDokumentTjeneste) {
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
    }

    public DokumentRestTjeneste() {
        // For Rest-CDI
    }

    @POST
    @Timed
    @Path("/forhandsvis-varselbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returnerer en pdf som er en forhåndsvisning av varselbrevet")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningVarselbrev(
            @ApiParam("Inneholder kode til brevmal og data som skal flettes inn i brevet") @Valid HentForhåndsvisningVarselbrevDto hentForhåndsvisningVarselbrevDto) { // NOSONAR
        byte[] dokument = bestillDokumentTjeneste.hentForhåndsvisningVarselbrev(hentForhåndsvisningVarselbrevDto);
        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type("application/pdf");
        responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
        return responseBuilder.build();
    }

    @POST
    @Timed
    @Path("/send-varsel")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Genererer varselbrev og sender det til Dokumentproduksjon. Kun til testing.")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response sendVarsel(@Valid SendVarselbrevDto sendVarselbrevDto) { // NOSONAR
        bestillDokumentTjeneste.sendVarselbrev(sendVarselbrevDto.getFagsakId(), sendVarselbrevDto.getAktørId(), sendVarselbrevDto.getBehandlingId());
        Response.ResponseBuilder responseBuilder = Response.ok();
        return responseBuilder.build();
    }

    @POST
    @Timed
    @Path("/hent-vedtaksbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returnerer forhåndsvisning av vedtaksbrevet som tekst, slik at det kan vises i GUI for redigering")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentVedtaksbrevForRedigering(@Valid BehandlingIdDto behandlingIdDto) { // NOSONAR
        ForhåndvisningVedtaksbrevTekstDto vedtaksbrevDto = bestillDokumentTjeneste.
                hentForhåndsvisningVedtaksbrevSomTekst(behandlingIdDto.getBehandlingId());
        return Response.ok(vedtaksbrevDto).build();
    }

    @POST
    @Timed
    @Path("/forhandsvis-vedtaksbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returnerer en pdf som er en forhåndsvisning av vedtaksbrevet")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningVedtaksbrev(@Valid HentForhåndvisningVedtaksbrevPdfDto vedtaksbrevPdfDto) { // NOSONAR
        byte[] dokument = bestillDokumentTjeneste.hentForhåndsvisningVedtaksbrevSomPdf(vedtaksbrevPdfDto);
        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type("application/pdf");
        responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
        return responseBuilder.build();
    }

    @POST
    @Timed
    @Path("/send-vedtak-test")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Genererer vedtaksbrev og sender det til Dokumentproduksjon. Kun til testing.")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response sendVarsel(@Valid HentForhåndvisningVedtaksbrevPdfDto vedtaksbrevPdfDto) { // NOSONAR
        bestillDokumentTjeneste.sendVedtaksbrevTest(vedtaksbrevPdfDto);
        Response.ResponseBuilder responseBuilder = Response.ok();
        return responseBuilder.build();
    }
}
