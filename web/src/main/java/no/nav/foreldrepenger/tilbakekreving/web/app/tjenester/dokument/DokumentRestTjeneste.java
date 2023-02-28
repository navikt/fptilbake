package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.dokument;

import java.util.List;
import java.util.function.Function;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.ForhåndvisningVedtaksbrevTekstDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningHenleggelseslbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndsvisningVarselbrevDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.HentForhåndvisningVedtaksbrevPdfDto;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.HenleggelsesbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.VarselbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.VedtaksbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/dokument")
@ApplicationScoped
public class DokumentRestTjeneste {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String FILENAME_DOKUMENT_PDF = "filename=dokument.pdf";
    private VarselbrevTjeneste varselbrevTjeneste;
    private VedtaksbrevTjeneste vedtaksbrevTjeneste;
    private HenleggelsesbrevTjeneste henleggelsesbrevTjeneste;
    private BehandlingTjeneste behandlingTjeneste;

    @Inject
    public DokumentRestTjeneste(VarselbrevTjeneste varselbrevTjeneste,
                                VedtaksbrevTjeneste vedtaksbrevTjeneste,
                                HenleggelsesbrevTjeneste henleggelsesbrevTjeneste,
                                BehandlingTjeneste behandlingTjeneste) {
        this.varselbrevTjeneste = varselbrevTjeneste;
        this.vedtaksbrevTjeneste = vedtaksbrevTjeneste;
        this.henleggelsesbrevTjeneste = henleggelsesbrevTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
    }

    public DokumentRestTjeneste() {
        // For Rest-CDI
    }

    @POST
    @Path("/forhandsvis-varselbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "dokument", description = "Returnerer en pdf som er en forhåndsvisning av varselbrevet")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningVarselbrev(@TilpassetAbacAttributt(supplierClass = ForhåndsvisningVarselbrev.class)
                                                  @Parameter(description = "Inneholder kode til brevmal og data som skal flettes inn i brevet") @Valid HentForhåndsvisningVarselbrevDto hentForhåndsvisningVarselbrevDto) { // NOSONAR
        byte[] dokument = varselbrevTjeneste.hentForhåndsvisningVarselbrev(hentForhåndsvisningVarselbrevDto);
        Response.ResponseBuilder responseBuilder = lagRespons(dokument);
        return responseBuilder.build();
    }

    @GET
    @Path("/hent-vedtaksbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "dokument", description = "Returnerer forhåndsvisning av vedtaksbrevet som tekst, slik at det kan vises i GUI for redigering")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public ForhåndvisningVedtaksbrevTekstDto hentVedtaksbrevForRedigering(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                                                          @NotNull @QueryParam("uuid") @Valid BehandlingReferanse behandlingReferanse) { // NOSONAR
        Long behandlingId = hentBehandlingId(behandlingReferanse);
        List<Avsnitt> avsnittene = vedtaksbrevTjeneste.hentForhåndsvisningVedtaksbrevSomTekst(behandlingId);
        return new ForhåndvisningVedtaksbrevTekstDto(avsnittene);
    }

    private Long hentBehandlingId(@QueryParam("behandlingUuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.erInternBehandlingId()
                ? behandlingReferanse.getBehandlingId()
                : behandlingTjeneste.hentBehandlingId(behandlingReferanse.getBehandlingUuid());
    }

    @POST
    @Path("/forhandsvis-vedtaksbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "dokument", description = "Returnerer en pdf som er en forhåndsvisning av vedtaksbrevet")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningVedtaksbrev(@TilpassetAbacAttributt(supplierClass = ForhåndvisningVedtaksbrevPdf.class) @Valid @NotNull HentForhåndvisningVedtaksbrevPdfDto vedtaksbrevPdfDto) { // NOSONAR
        byte[] dokument = vedtaksbrevTjeneste.hentForhåndsvisningVedtaksbrevMedVedleggSomPdf(vedtaksbrevPdfDto);
        Response.ResponseBuilder responseBuilder = lagRespons(dokument);
        return responseBuilder.build();
    }

    @POST
    @Path("/forhandsvis-henleggelsesbrev")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "dokument", description = "Returnerer en pdf som er en forhåndsvisning av henleggelsesbrevet")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningHenleggelsesbrev(@TilpassetAbacAttributt(supplierClass = ForhåndsvisningHenleggelseslbrev.class) @Valid @NotNull HentForhåndsvisningHenleggelseslbrevDto henleggelseslbrevDto) { // NOSONAR
        byte[] dokument;
        BehandlingReferanse behandlingReferanse = henleggelseslbrevDto.getBehandlingReferanse();
        String fritekst = henleggelseslbrevDto.getFritekst();
        if (behandlingReferanse.erInternBehandlingId()) {
            dokument = henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(behandlingReferanse.getBehandlingId(), fritekst);
        } else {
            dokument = henleggelsesbrevTjeneste.hentForhåndsvisningHenleggelsebrev(behandlingReferanse.getBehandlingUuid(), fritekst);
        }
        Response.ResponseBuilder responseBuilder = lagRespons(dokument);
        return responseBuilder.build();
    }

    private Response.ResponseBuilder lagRespons(byte[] dokument) {
        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type(PDF_CONTENT_TYPE);
        responseBuilder.header(CONTENT_DISPOSITION, FILENAME_DOKUMENT_PDF);
        return responseBuilder;
    }

    public static class ForhåndsvisningVarselbrev implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (HentForhåndsvisningVarselbrevDto) obj;
            return AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.YTELSEBEHANDLING_UUID, req.getBehandlingUuid());
        }
    }

    public static class ForhåndsvisningHenleggelseslbrev implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (HentForhåndsvisningHenleggelseslbrevDto) obj;
            return BehandlingReferanseAbacAttributter.fraBehandlingReferanse(req.getBehandlingReferanse());
        }
    }

    public static class ForhåndvisningVedtaksbrevPdf implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (HentForhåndvisningVedtaksbrevPdfDto) obj;
            return BehandlingReferanseAbacAttributter.fraBehandlingReferanse(req.getBehandlingReferanse());
        }
    }


}
