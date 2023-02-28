package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons;

import java.util.Optional;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path(value = "/varsel/respons")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@RequestScoped
@Transactional
public class VarselresponsRestTjeneste {

    private VarselresponsTjeneste responsTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    public VarselresponsRestTjeneste() {
    }

    @Inject
    public VarselresponsRestTjeneste(VarselresponsTjeneste responsTjeneste, GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste) {
        this.responsTjeneste = responsTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
    }

    @GET
    @Operation(
            tags = "brukerrespons",
            description = "Henter respons for behandling",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respons lagret", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VarselresponsDto.class))),
                    @ApiResponse(responseCode = "404", description = "Response finnes ikke")
            })
    @Path(value = "/hent-respons")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response finnRespons(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                @Valid @NotNull @QueryParam("behandlingId") BehandlingReferanse behandlingReferanse) {
        Optional<VarselresponsDto> responsDto = responsTjeneste.hentRespons(behandlingReferanse.getBehandlingId()).map(VarselresponsDto::fraDomene);
        if (responsDto.isPresent()) {
            return Response.ok(responsDto.get()).build();
        }
        return Response.noContent().build();
    }

    @POST
    @Operation(
            tags = "brukerrespons",
            description = "Lagrer respons fra bruker",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respons registrert")
            })
    @Path(value = "/registrer")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, property = AbacProperty.FAGSAK)
    public Response registrerBrukerrespons(@Valid @NotNull VarselresponsDto brukerRespons) {
        responsTjeneste.lagreRespons(brukerRespons.getBehandlingId(), ResponsKanal.SELVBETJENING, brukerRespons.getAkseptertFaktagrunnlag());
        gjenopptaBehandlingTjeneste.fortsettBehandling(brukerRespons.getBehandlingId());
        return Response.ok().build();
    }
}
