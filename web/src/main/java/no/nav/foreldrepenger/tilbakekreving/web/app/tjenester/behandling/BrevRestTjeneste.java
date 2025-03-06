package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler.DokumentBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BestillBrevDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path(BrevRestTjeneste.BASE_PATH)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
@Transactional
public class BrevRestTjeneste {

    public static final String BASE_PATH = "/brev";

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
    @Path("/maler")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "brev", description = "Henter liste over tilgjengelige brevtyper")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    public List<BrevmalDto> hentMaler(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                      @Valid @QueryParam("uuid") BehandlingReferanse behandlingReferanse) {
        var behandlingId = hentBehandlingId(behandlingReferanse);
        return dokumentBehandlingTjeneste.hentBrevmalerFor(behandlingId);
    }

    @POST
    @Path("/bestill")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "brev", description = "bestiller brev")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public Response bestillBrev(@NotNull @Valid BestillBrevDto bestillBrevDto) {
        var malType = DokumentMalType.fraKode(bestillBrevDto.getBrevmalkode());
        var behandlingId = hentBehandlingId(bestillBrevDto.getBehandlingReferanse());
        dokumentBehandlingTjeneste.bestillBrev(behandlingId, malType, bestillBrevDto.getFritekst());
        return Response.ok().build();
    }

    @POST
    @Path("/forhandsvis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "brev", description = "Returnerer en pdf som er en forhåndsvisning av brevet")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public Response forhåndsvisBrev(@Parameter(description = "Inneholder kode til brevmal og data som skal flettes inn i brevet") @NotNull @Valid BestillBrevDto forhåndsvisBestillBrevDto) {
        var malType = DokumentMalType.fraKode(forhåndsvisBestillBrevDto.getBrevmalkode());
        var fritekst = forhåndsvisBestillBrevDto.getFritekst();
        var behandlingId = hentBehandlingId(forhåndsvisBestillBrevDto.getBehandlingReferanse());
        var dokument = dokumentBehandlingTjeneste.forhåndsvisBrev(behandlingId, malType, fritekst);

        var responseBuilder = Response.ok(dokument);
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
