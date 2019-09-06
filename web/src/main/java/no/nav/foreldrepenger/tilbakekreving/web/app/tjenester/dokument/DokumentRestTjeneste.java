package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.dokument;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.VarselbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.VedtaksbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.ForhåndvisningVedtaksbrevTekstDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndvisningVedtaksbrevPdfDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.SendVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "dokument")
@Path("/dokument")
@ApplicationScoped
public class DokumentRestTjeneste {

    private VarselbrevTjeneste varselbrevTjeneste;
    private VedtaksbrevTjeneste vedtaksbrevTjeneste;

    @Inject
    public DokumentRestTjeneste(VarselbrevTjeneste varselbrevTjeneste, VedtaksbrevTjeneste vedtaksbrevTjeneste) {
        this.varselbrevTjeneste = varselbrevTjeneste;
        this.vedtaksbrevTjeneste = vedtaksbrevTjeneste;
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
        byte[] dokument = varselbrevTjeneste.hentForhåndsvisningVarselbrev(hentForhåndsvisningVarselbrevDto);
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
        Long fagsakId = sendVarselbrevDto.getFagsakId();
        Long behandlingId = sendVarselbrevDto.getBehandlingId();
        AktørId aktørId = new AktørId(sendVarselbrevDto.getAktørId());
        varselbrevTjeneste.sendVarselbrev(fagsakId, aktørId, behandlingId);
        return Response.ok().build();
    }

    @POST
    @Timed
    @Path("/hent-vedtaksbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returnerer forhåndsvisning av vedtaksbrevet som tekst, slik at det kan vises i GUI for redigering")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public ForhåndvisningVedtaksbrevTekstDto hentVedtaksbrevForRedigering(@Valid BehandlingIdDto behandlingIdDto) { // NOSONAR
        List<Avsnitt> avsnittene = vedtaksbrevTjeneste.hentForhåndsvisningVedtaksbrevSomTekst(behandlingIdDto.getBehandlingId());
        return new ForhåndvisningVedtaksbrevTekstDto(avsnittene);
    }

    @POST
    @Timed
    @Path("/forhandsvis-vedtaksbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returnerer en pdf som er en forhåndsvisning av vedtaksbrevet")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningVedtaksbrev(@Valid HentForhåndvisningVedtaksbrevPdfDto vedtaksbrevPdfDto) { // NOSONAR
        byte[] dokument = vedtaksbrevTjeneste.hentForhåndsvisningVedtaksbrevSomPdf(vedtaksbrevPdfDto);
        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type("application/pdf");
        responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
        return responseBuilder.build();
    }

}
