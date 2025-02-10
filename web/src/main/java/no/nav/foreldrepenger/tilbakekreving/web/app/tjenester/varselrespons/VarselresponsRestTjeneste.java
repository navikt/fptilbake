package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path(value = "/varsel/respons")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@RequestScoped
@Transactional
@Deprecated(forRemoval = true)
public class VarselresponsRestTjeneste {

    //SKAL SLETTES

    private VarselresponsTjeneste responsTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    public VarselresponsRestTjeneste() {
    }

    @Inject
    public VarselresponsRestTjeneste(VarselresponsTjeneste responsTjeneste, GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste) {
        this.responsTjeneste = responsTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
    }

    @POST
    @Operation(
            tags = "brukerrespons",
            description = "Lagrer respons fra bruker",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respons registrert")
            })
    @Path(value = "/registrer")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response registrerBrukerrespons(@Valid @NotNull VarselresponsDto brukerRespons) {
        //Brukes av fp-autotest. Ikke bruk denne. Skal slettes
        responsTjeneste.lagreRespons(brukerRespons.getBehandlingId(), ResponsKanal.SELVBETJENING);
        gjenopptaBehandlingTjeneste.fortsettBehandling(brukerRespons.getBehandlingId());
        return Response.ok().build();
    }
}
