package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.varselrespons;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "brukerrespons")
@Path(value = "/varsel/respons")
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@RequestScoped
@Transaction
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

    @POST
    @ApiOperation(value = "Lagrer respons fra bruker")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Respons registrert")
    })
    @Path(value = "/registrer")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public Response registrerBrukerrespons(@Valid @NotNull VarselresponsDto brukerRespons) {
        responsTjeneste.lagreRespons(brukerRespons.getBehandlingId(), ResponsKanal.SELVBETJENING, brukerRespons.getAkseptertFaktagrunnlag());
        gjenopptaBehandlingTjeneste.fortsettBehandling(brukerRespons.getBehandlingId());
        return Response.ok().build();
    }

    @GET
    @ApiOperation(value = "Henter respons for behandling")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Respons lagret", response = VarselresponsDto.class),
        @ApiResponse(code = 404, message = "Response finnes ikke")
    })
    @Path(value = "/hent-respons")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response finnRespons(@Valid @NotNull @QueryParam("behandlingId") BehandlingIdDto behandlingIdDto) {
        Optional<VarselresponsDto> responsDto = responsTjeneste.hentRespons(behandlingIdDto.getBehandlingId()).map(VarselresponsDto::fraDomene);
        if (responsDto.isPresent()) {
            return Response.ok(responsDto.get()).build();
        }
        return Response.noContent().build();
    }

}
