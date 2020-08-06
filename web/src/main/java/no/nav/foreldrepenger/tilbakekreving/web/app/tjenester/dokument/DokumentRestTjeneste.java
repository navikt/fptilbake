package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.dokument;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.ForhåndvisningVedtaksbrevTekstDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndvisningVedtaksbrevPdfDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.HenleggelsesbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.VedtaksbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/dokument")
@ApplicationScoped
public class DokumentRestTjeneste {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String FILENAME_DOKUMENT_PDF = "filename=dokument.pdf";
    private VarselbrevTjeneste varselbrevTjeneste;
    private VedtaksbrevTjeneste vedtaksbrevTjeneste;
    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste;

    @Inject
    public DokumentRestTjeneste(VarselbrevTjeneste varselbrevTjeneste, VedtaksbrevTjeneste vedtaksbrevTjeneste, HenleggelsesbrevTjeneste henleggelsesbrevTjeneste) {
        this.varselbrevTjeneste = varselbrevTjeneste;
        this.vedtaksbrevTjeneste = vedtaksbrevTjeneste;
        this.henleggelsesbrevTjeneste = henleggelsesbrevTjeneste;
    }

    public DokumentRestTjeneste() {
        // For Rest-CDI
    }

    @POST
    @Timed
    @Path("/forhandsvis-varselbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "dokument", description = "Returnerer en pdf som er en forhåndsvisning av varselbrevet")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningVarselbrev(
        @Parameter(description = "Inneholder kode til brevmal og data som skal flettes inn i brevet") @Valid HentForhåndsvisningVarselbrevDto hentForhåndsvisningVarselbrevDto) { // NOSONAR
        byte[] dokument = varselbrevTjeneste.hentForhåndsvisningVarselbrev(hentForhåndsvisningVarselbrevDto);
        Response.ResponseBuilder responseBuilder = lagRespons(dokument);
        return responseBuilder.build();
    }

    @GET
    @Timed
    @Path("/hent-vedtaksbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "dokument", description = "Returnerer forhåndsvisning av vedtaksbrevet som tekst, slik at det kan vises i GUI for redigering")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public ForhåndvisningVedtaksbrevTekstDto hentVedtaksbrevForRedigering(@NotNull @QueryParam ("behandlingId") @Valid  BehandlingIdDto behandlingIdDto) { // NOSONAR
        List<Avsnitt> avsnittene = vedtaksbrevTjeneste.hentForhåndsvisningVedtaksbrevSomTekst(behandlingIdDto.getBehandlingId());
        return new ForhåndvisningVedtaksbrevTekstDto(avsnittene);
    }

    @POST
    @Timed
    @Path("/forhandsvis-vedtaksbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "dokument", description = "Returnerer en pdf som er en forhåndsvisning av vedtaksbrevet")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningVedtaksbrev(@Valid @NotNull HentForhåndvisningVedtaksbrevPdfDto vedtaksbrevPdfDto) { // NOSONAR
        byte[] dokument = vedtaksbrevTjeneste.hentForhåndsvisningVedtaksbrevMedVedleggSomPdf(vedtaksbrevPdfDto);
        Response.ResponseBuilder responseBuilder = lagRespons(dokument);
        return responseBuilder.build();
    }

    @POST
    @Timed
    @Path("/forhandsvis-henleggelsesbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "dokument", description = "Returnerer en pdf som er en forhåndsvisning av henleggelsesbrevet")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningHenleggelsesbrev(@Valid @NotNull BehandlingIdDto behandlingIdDto) { // NOSONAR

        byte[] dokument = henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(behandlingIdDto.getBehandlingId());

        Response.ResponseBuilder responseBuilder = lagRespons(dokument);
        return responseBuilder.build();
    }

    private Response.ResponseBuilder lagRespons(byte[] dokument) {
        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type(PDF_CONTENT_TYPE);
        responseBuilder.header(CONTENT_DISPOSITION, FILENAME_DOKUMENT_PDF);
        return responseBuilder;
    }

}
