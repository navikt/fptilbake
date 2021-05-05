package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler.DokumentBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BestillBrevDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Path(BrevRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
@Transactional
public class BrevRestTjeneste {

    public static final String PATH_FRAGMENT = "/brev"; // NOSONAR

    private DokumentBehandlingTjeneste dokumentBehandlingTjeneste;
    private BehandlingTjeneste behandlingTjeneste;

    public BrevRestTjeneste() {
        // CDI
    }

    @Inject
    public BrevRestTjeneste(DokumentBehandlingTjeneste dokumentBehandlingTjeneste,
                            BehandlingTjeneste behandlingTjeneste) {
        this.dokumentBehandlingTjeneste = dokumentBehandlingTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
    }


    @GET
    @Timed
    @Path("/maler")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Operation(tags = "brev", description = "Henter liste over tilgjengelige brevtyper")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BrevmalDto> hentMaler(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                          @Valid @QueryParam("uuid") BehandlingReferanse behandlingReferanse) {
        long behandlingId = hentBehandlingId(behandlingReferanse);
        return dokumentBehandlingTjeneste.hentBrevmalerFor(behandlingId);
    }

    @POST
    @Timed
    @Path("/bestill")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Operation(tags = "brev", description = "bestiller brev")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response bestillBrev(@NotNull @Valid BestillBrevDto bestillBrevDto) {
        DokumentMalType malType = DokumentMalType.fraKode(bestillBrevDto.getBrevmalkode());
        long behandlingId = hentBehandlingId(bestillBrevDto.getBehandlingReferanse());
        dokumentBehandlingTjeneste.bestillBrev(behandlingId, malType, bestillBrevDto.getFritekst());
        return Response.ok().build();
    }

    @POST
    @Timed
    @Path("/forhandsvis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "brev", description = "Returnerer en pdf som er en forhåndsvisning av brevet")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response forhåndsvisBrev(@Parameter(description = "Inneholder kode til brevmal og data som skal flettes inn i brevet") @NotNull @Valid BestillBrevDto forhåndsvisBestillBrevDto) {
        DokumentMalType malType = DokumentMalType.fraKode(forhåndsvisBestillBrevDto.getBrevmalkode());
        String fritekst = forhåndsvisBestillBrevDto.getFritekst();
        long behandlingId = hentBehandlingId(forhåndsvisBestillBrevDto.getBehandlingReferanse());
        byte[] dokument = dokumentBehandlingTjeneste.forhåndsvisBrev(behandlingId, malType, fritekst);

        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type("application/pdf");
        responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
        return responseBuilder.build();
    }

    private long hentBehandlingId(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.erInternBehandlingId()
            ? behandlingReferanse.getBehandlingId()
            : behandlingTjeneste.hentBehandlingId(behandlingReferanse.getBehandlingUuid());
    }

}
