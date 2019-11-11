package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.dokument;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.ForhåndvisningVedtaksbrevTekstDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningManueltVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndvisningVedtaksbrevPdfDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt.ManueltVarselBrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.VedtaksbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "dokument")
@Path("/dokument")
@ApplicationScoped
public class DokumentRestTjeneste {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String FILENAME_DOKUMENT_PDF = "filename=dokument.pdf";
    private VarselbrevTjeneste varselbrevTjeneste;
    private VedtaksbrevTjeneste vedtaksbrevTjeneste;
    private ManueltVarselBrevTjeneste manueltVarselBrevTjeneste;

    @Inject
    public DokumentRestTjeneste(VarselbrevTjeneste varselbrevTjeneste, VedtaksbrevTjeneste vedtaksbrevTjeneste, ManueltVarselBrevTjeneste manueltVarselBrevTjeneste) {
        this.varselbrevTjeneste = varselbrevTjeneste;
        this.vedtaksbrevTjeneste = vedtaksbrevTjeneste;
        this.manueltVarselBrevTjeneste = manueltVarselBrevTjeneste;
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
        responseBuilder.type(PDF_CONTENT_TYPE);
        responseBuilder.header(CONTENT_DISPOSITION, FILENAME_DOKUMENT_PDF);
        return responseBuilder.build();
    }

    @POST
    @Timed
    @Path("/forhandsvis-manueltvarselbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returnerer en pdf som er en forhåndsvisning av manuelt varselbrevet")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningManuletVarselbrev(
        @ApiParam("Inneholder kode til brevmal og data som skal flettes inn i brevet") @NotNull  @Valid HentForhåndsvisningManueltVarselbrevDto forhåndsvisningManueltVarselbrevDto) { // NOSONAR
        DokumentMalType malType = DokumentMalType.fraKode(forhåndsvisningManueltVarselbrevDto.getDokumentMalType());
        String fritekst = forhåndsvisningManueltVarselbrevDto.getFriTekst();
        long behandlingId = forhåndsvisningManueltVarselbrevDto.getBehandlingId();
        byte[] dokument =  manueltVarselBrevTjeneste.hentForhåndsvisningManueltVarselbrev(behandlingId, malType, fritekst);
        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type(PDF_CONTENT_TYPE);
        responseBuilder.header(CONTENT_DISPOSITION, FILENAME_DOKUMENT_PDF);
        return responseBuilder.build();
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
        responseBuilder.type(PDF_CONTENT_TYPE);
        responseBuilder.header(CONTENT_DISPOSITION, FILENAME_DOKUMENT_PDF);
        return responseBuilder.build();
    }

}
