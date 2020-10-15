package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.batch;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import javax.enterprise.context.ApplicationScoped;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.batch.feil.BatchFeil;
import no.nav.foreldrepenger.batch.impl.BatchSupportTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.batch.args.BatchArgumentsDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/batch")
@ApplicationScoped
@Transactional
public class BatchRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(BatchRestTjeneste.class);

    private BatchSupportTjeneste batchSupportTjeneste;

    public BatchRestTjeneste() {
        // For CDI
    }

    @Inject
    public BatchRestTjeneste(BatchSupportTjeneste batchSupportTjeneste) {
        this.batchSupportTjeneste = batchSupportTjeneste;
    }

    private static String retrieveBatchServiceFrom(String executionId) {
        if (executionId != null && executionId.contains("-")) {
            return executionId.substring(0, executionId.indexOf('-'));
        }
        throw new IllegalStateException();
    }

    /**
     * Kalles på for å logge brukeren inn i løsningen. Dette for å ha minimalt med innloggingslogikk i bash-scriptet
     *
     * @return alltid 200 - OK
     */
    @GET
    @Path("/init")
    @BeskyttetRessurs(action = READ, property = AbacProperty.DRIFT, sporingslogg = false)
    @Operation(tags = "batch", description = "denne gjør ingenting - brukes for å logge inn bruker som kjører batch")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response init() {
        return Response.ok().build();
    }

    @POST
    @Timed
    @Path("/launch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(tags = "batch", description = "Start batchjob", responses = {
        @ApiResponse(responseCode = "200", description = "Starter batch og returnerer executionId"),
        @ApiResponse(responseCode = "400", description = "Ukjent batch forespurt."),
        @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = CREATE, property = AbacProperty.BATCH)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response startBatch(@NotNull @QueryParam("batchName") @Valid BatchNameDto batchName, @Valid BatchArgumentsDto args) {
        String name = batchName.getVerdi();
        final BatchTjeneste batchTjeneste = batchSupportTjeneste.finnBatchTjenesteForNavn(name);
        if (batchTjeneste != null) {
            final BatchArguments arguments = batchTjeneste.createArguments(args.getArguments());
            if (arguments.isValid()) {
                logger.info("Starter batch {}", LoggerUtils.removeLineBreaks(name)); // NOSONAR
                return Response.ok(batchTjeneste.launch(arguments)).build();
            } else {
                throw BatchFeil.FACTORY.ugyldigeJobParametere(arguments).toException();
            }
        }
        BatchFeil.FACTORY.ugyldiJobbNavnOppgitt(name).log(logger);
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Timed
    @Path("/poll")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(tags = "batch", description = "Poll status of batchjob", responses = {
        @ApiResponse(responseCode = "200", description = "Henter ut exitkode for executionId", content = @Content(mediaType = "text")),
        @ApiResponse(responseCode = "400", description = "Ukjent batch forespurt."),
        @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = READ, property = AbacProperty.DRIFT, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response poll(@NotNull @QueryParam("executionId") @Valid BatchExecutionDto dto) {
        final String batchName = retrieveBatchServiceFrom(dto.getExecutionId());
        final BatchTjeneste batchTjeneste = batchSupportTjeneste.finnBatchTjenesteForNavn(batchName);
        if (batchTjeneste != null) {
            return Response.ok(batchTjeneste.status(dto.getExecutionId()).value()).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }


    @POST
    @Timed
    @Path("/autorun")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(tags = "batch", description = "Start task for å kjøre batchjobs", responses = {
        @ApiResponse(responseCode = "200", description = "Starter batch-scheduler."),
        @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = CREATE, property = AbacProperty.DRIFT, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response autoRunBatch() {
        batchSupportTjeneste.startBatchSchedulerTask();
        return Response.ok().build();
    }

}
