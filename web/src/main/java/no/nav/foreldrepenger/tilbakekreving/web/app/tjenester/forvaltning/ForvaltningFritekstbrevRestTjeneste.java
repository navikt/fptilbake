package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekst.FritekstbrevTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekst.SendFritekstbrevTask;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.util.InputValideringRegex;

@Path("/forvaltningBrev")
@ApplicationScoped
@Transactional
public class ForvaltningFritekstbrevRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(ForvaltningFritekstbrevRestTjeneste.class);

    private FritekstbrevTjeneste fritekstbrevTjeneste;
    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingRepository behandlingRepository;

    public ForvaltningFritekstbrevRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningFritekstbrevRestTjeneste(FritekstbrevTjeneste fritekstbrevTjeneste, ProsessTaskRepository prosessTaskRepository, BehandlingRepository behandlingRepository) {
        this.fritekstbrevTjeneste = fritekstbrevTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
        this.behandlingRepository = behandlingRepository;
    }

    public static class FritekstbrevDto implements AbacDto {
        @Valid
        @Min(0)
        @Max(4000000)
        private Long behandlingId;

        @NotNull
        @Pattern(regexp = InputValideringRegex.FRITEKST)
        @Size(min = 3, max = 4000)
        private String tittel;

        @NotNull
        @Pattern(regexp = InputValideringRegex.FRITEKST)
        @Size(min = 3, max = 4000)
        private String overskrift;

        @NotNull
        @Pattern(regexp = InputValideringRegex.FRITEKST)
        @Size(min = 3, max = 4000)
        private String fritekst;

        @NotNull
        private BrevMottaker mottaker;

        public Long getBehandlingId() {
            return behandlingId;
        }

        public String getTittel() {
            return tittel;
        }

        public String getOverskrift() {
            return overskrift;
        }

        public String getFritekst() {
            return fritekst;
        }

        public BrevMottaker getMottaker() {
            return mottaker;
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
        }
    }

    @POST
    @Path("/forhåndsvis-fritekst-brev")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(tags = "FORVALTNING-brev", description = "Tjeneste for å forhåndsvise et fritekstbrev.")
    //ingen sporingslogg siden ingen data for bruker vises
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, property = AbacProperty.DRIFT, sporingslogg = false)
    public Response forhåndsvisBrev(@Valid @NotNull FritekstbrevDto dto) {
        Behandling behandling = behandlingRepository.hentBehandling(dto.getBehandlingId());
        byte[] dokument = fritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(behandling, dto.getTittel(), dto.getOverskrift(), dto.getFritekst());
        Response.ResponseBuilder responseBuilder = lagRespons(dokument);
        return responseBuilder.build();
    }

    @POST
    @Path("/send-fritekst-brev")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(tags = "FORVALTNING-brev", description = "Tjeneste for å sende et fritekstbrev.")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, property = AbacProperty.DRIFT)
    public Response sendBrev(@Valid @NotNull FritekstbrevDto dto) {
        ProsessTaskData task = new ProsessTaskData(SendFritekstbrevTask.TASKTYPE);
        task.setPayload(dto.getFritekst());
        task.setProperty("behandlingId", Long.toString(dto.getBehandlingId()));
        task.setProperty("tittel", base64encode(dto.getTittel()));
        task.setProperty("overskrift", base64encode(dto.getOverskrift()));
        String taskId = prosessTaskRepository.lagre(task);
        logger.info("Opprettet task med id={} for utsending av fritekstbrev for behandlingId={}  til {}", taskId, dto.getBehandlingId(), dto.getMottaker());
        return Response.ok().build();
    }

    private static String base64encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private Response.ResponseBuilder lagRespons(byte[] dokument) {
        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type("application/pdf");
        responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
        return responseBuilder;
    }


}
