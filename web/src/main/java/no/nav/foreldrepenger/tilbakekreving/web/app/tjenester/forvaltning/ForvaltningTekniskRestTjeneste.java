package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.PoststedKodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.poststed.PostnummerSynkroniseringTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.rest.AbacEmptySupplier;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskIdDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Path("/forvaltningTeknisk")
@ApplicationScoped
@Transactional
public class ForvaltningTekniskRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(ForvaltningTekniskRestTjeneste.class);

    private ProsessTaskRepository prosessTaskRepository;
    private PostnummerSynkroniseringTjeneste postnummerTjeneste;
    private PoststedKodeverkRepository poststedKodeverkRepository;

    public ForvaltningTekniskRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningTekniskRestTjeneste(ProsessTaskRepository prosessTaskRepository,
                                          PostnummerSynkroniseringTjeneste postnummerTjeneste,
                                          PoststedKodeverkRepository poststedKodeverkRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.postnummerTjeneste = postnummerTjeneste;
        this.poststedKodeverkRepository = poststedKodeverkRepository;
    }

    @PUT
    @Path("/sett-task-ferdig")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(tags = "FORVALTNING-teknisk", description = "Tjeneste for å tvinge en eksisterende prosess task til status FERDIG.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Task satt til ferdig."),
            @ApiResponse(responseCode = "400", description = "Fant ikke aktuell prosessTask."),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, property = AbacProperty.DRIFT)
    public Response setTaskFerdig(@Parameter(description = "Task som skal settes ferdig") @NotNull @TilpassetAbacAttributt(supplierClass = AbacEmptySupplier.class) @Valid ProsessTaskIdDto prosessTaskIdDto) {
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

    @POST
    @Path("/synk-postnummer")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hente og lagre kodeverk Postnummer", tags = "FORVALTNING-teknisk")
    @BeskyttetRessurs(action = CREATE, property = AbacProperty.DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response synkPostnummer() {
        postnummerTjeneste.synkroniserPostnummer();
        return Response.ok().build();
    }

    @GET
    @Path("/hent-postnummer")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hente lokale Postnummer", tags = "FORVALTNING-teknisk")
    @BeskyttetRessurs(action = CREATE, property = AbacProperty.DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentPostnummer() {
        return Response.ok(poststedKodeverkRepository.finnPostnummer("SYNK")).build();
    }
}
