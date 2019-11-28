package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskIdDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;

@Api(tags = {"FORVALTNING-teknisk"})
@Path("/forvaltningTeknisk")
@ApplicationScoped
@Transaction
public class ForvaltningTekniskRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(ForvaltningTekniskRestTjeneste.class);

    private ProsessTaskRepository prosessTaskRepository;

    public ForvaltningTekniskRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningTekniskRestTjeneste(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @PUT
    @Path("/sett-task-ferdig")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Tjeneste for å tvinge en eksisterende prosess task til status FERDIG.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Task satt til ferdig."),
        @ApiResponse(code = 400, message = "Fant ikke aktuell prosessTask."),
        @ApiResponse(code = 500, message = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.DRIFT)
    public Response setTaskFerdig(@ApiParam("Task som skal settes ferdig") @NotNull @Valid ProsessTaskIdDto prosessTaskIdDto) {
        ProsessTaskData data = prosessTaskRepository.finn(prosessTaskIdDto.getProsessTaskId());
        if (data != null) {
            data.setStatus(ProsessTaskStatus.FERDIG);
            data.setSisteFeil(null);
            data.setSisteFeilKode(null);
            prosessTaskRepository.lagre(data);
            logger.info("Prosesstask status med id {} er oppdatert til ferdig", prosessTaskIdDto.getProsessTaskId());
            return Response.ok().build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
