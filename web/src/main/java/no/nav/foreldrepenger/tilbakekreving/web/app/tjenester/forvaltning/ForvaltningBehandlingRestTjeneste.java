package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.DRIFT;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/forvaltningBehandling")
@ApplicationScoped
@Transaction
public class ForvaltningBehandlingRestTjeneste {

    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;

    public ForvaltningBehandlingRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningBehandlingRestTjeneste(BehandlingRepository behandlingRepository,
                                             ProsessTaskRepository prosessTaskRepository) {
        this.behandlingRepository = behandlingRepository;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @POST
    @Path("/tving-henleggelse")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å tvinge en behandling til å bli henlagt, selvom normale regler for saksbehandling ikke tillater henleggelse",
        responses = {
            @ApiResponse(responseCode = "200", description = "Henlagt behandling", content = @Content(mediaType = "text")),
            @ApiResponse(responseCode = "400", description = "Behandlingen er avsluttet"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    public Response tvingHenleggelseBehandling(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto behandlingIdDto) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingIdDto.getBehandlingId());
        if (behandling.erAvsluttet()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        opprettHenleggBehandlingTask(behandling);
        return Response.ok().build();
    }

    @POST
    @Path("/tving-gjenoppta")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å tvinge en behandling til å gjenopptas (tas av vent). NB! Må ikke brukes på saker uten kravgrunnlag!",
        responses = {
            @ApiResponse(responseCode = "200", description = "Gjenopptatt behandling", content = @Content(mediaType = "text")),
            @ApiResponse(responseCode = "400", description = "Behandlingen er avsluttet eller behandlingen er ikke på vent"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    public Response tvingGjenopptaBehandling(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto behandlingIdDto) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingIdDto.getBehandlingId());
        if (behandling.erAvsluttet() || !behandling.isBehandlingPåVent()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        opprettGjenopptaBehandlingTask(behandling);

        return Response.ok().build();
    }

    private void opprettGjenopptaBehandlingTask(Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(GjenopptaBehandlingTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskRepository.lagre(prosessTaskData);
    }

    private void opprettHenleggBehandlingTask(Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(TvingHenlegglBehandlingTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskRepository.lagre(prosessTaskData);
    }

}
