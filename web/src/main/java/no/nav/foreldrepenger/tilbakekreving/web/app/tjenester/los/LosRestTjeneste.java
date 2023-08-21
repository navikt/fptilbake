package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.los;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.hendelser.behandling.los.LosBehandlingDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@ApplicationScoped
@Transactional
@Path(LosRestTjeneste.BASE_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class LosRestTjeneste {

    static final String BASE_PATH = "/los";

    private static final String LOS_BEHANDLING_PATH = "/los-behandling";

    private BehandlingRepository behandlingRepository;
    private LosBehandlingDtoTjeneste losBehandlingDtoTjeneste;

    public LosRestTjeneste() {
        // CDI
    }

    @Inject
    public LosRestTjeneste(BehandlingRepository behandlingRepository,
                           LosBehandlingDtoTjeneste losBehandlingDtoTjeneste) {
        this.losBehandlingDtoTjeneste = losBehandlingDtoTjeneste;
        this.behandlingRepository = behandlingRepository;
    }

    @GET
    @Path(LOS_BEHANDLING_PATH)
    @Operation(description = "Hent behandling gitt id for LOS", summary = ("Returnerer behandlingen som er tilknyttet uuid."), tags = "los-data", responses = {
            @ApiResponse(responseCode = "200", description = "Returnerer behandling", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = LosBehandlingDto.class))
            }),
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response hentBehandlingForLos(@NotNull @QueryParam(UuidDto.NAME) @Parameter(description = UuidDto.DESC) @Valid UuidDto uuidDto) {
        var behandling = behandlingRepository.hentBehandling(uuidDto.getBehandlingUuid());
        var dto = losBehandlingDtoTjeneste.lagLosBehandlingDto(behandling);
        var responseBuilder = Response.ok().entity(dto);
        return responseBuilder.build();
    }

}
