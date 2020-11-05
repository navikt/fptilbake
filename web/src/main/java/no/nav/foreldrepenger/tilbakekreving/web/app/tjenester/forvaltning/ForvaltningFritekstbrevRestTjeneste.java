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
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
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

    public static class BehandlingIdDto implements AbacDto {
        @Valid
        @Min(0)
        @Max(4000000)
        @QueryParam("behandlingId")
        private Long behandlingId;

        public Long getBehandlingId() {
            return behandlingId;
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
        }
    }

    public static class BehandlingIdOgMottakerDto implements AbacDto {
        @Valid
        @Min(0)
        @Max(4000000)
        private Long behandlingId;

        @NotNull
        @Valid
        private BrevMottaker mottaker;

        public Long getBehandlingId() {
            return behandlingId;
        }

        public BrevMottaker getMottaker() {
            return mottaker;
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
        }
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
        @Valid
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
    @Path("/forhaandsvis-fritekst-brev")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(tags = "FORVALTNING-brev", description = "Tjeneste for å forhåndsvise et fritekstbrev.")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, property = AbacProperty.DRIFT)
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
        task.setProperty("mottaker", dto.getMottaker().name());
        String taskId = prosessTaskRepository.lagre(task);
        logger.info("Opprettet task med id={} for utsending av fritekstbrev for behandlingId={}  til {}", taskId, dto.getBehandlingId(), dto.getMottaker());
        return Response.ok().build();
    }

    @GET
    @Path("/forhaandsvis-brev-feilutsendt-varsel")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(tags = "FORVALTNING-brev", description = "Tjeneste for å forhåndsvise brev ang feilutsendt varsel.")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, property = AbacProperty.DRIFT)
    public Response forhåndsvisBrevFeilutsendtVarselGet(@Parameter(description = "BehandlingId") @BeanParam @Valid @NotNull BehandlingIdDto behandligId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandligId.getBehandlingId());
        FagsakYtelseType ytelseType = behandling.getFagsak().getFagsakYtelseType();

        byte[] dokument = fritekstbrevTjeneste.hentForhåndsvisningFritekstbrev(behandling, getTittelFeilutesendtVarselbrev(), getOverskriftFeilutesendtVarselbrev(ytelseType), getInnholdFeilutsendtVarselbrev(ytelseType));
        Response.ResponseBuilder responseBuilder = lagRespons(dokument);
        return responseBuilder.build();
    }

    @POST
    @Path("/send-feilutsendt-varsel")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(tags = "FORVALTNING-brev", description = "Tjeneste for å sende brev ang feilutsendt varsel.")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, property = AbacProperty.DRIFT)
    public Response sendBrev(@Valid @NotNull BehandlingIdOgMottakerDto dto) {
        Behandling behandling = behandlingRepository.hentBehandling(dto.getBehandlingId());
        FagsakYtelseType ytelseType = behandling.getFagsak().getFagsakYtelseType();

        ProsessTaskData task = new ProsessTaskData(SendFritekstbrevTask.TASKTYPE);
        task.setPayload(getInnholdFeilutsendtVarselbrev(ytelseType));
        task.setProperty("behandlingId", Long.toString(dto.getBehandlingId()));
        task.setProperty("tittel", base64encode(getTittelFeilutesendtVarselbrev()));
        task.setProperty("overskrift", base64encode(getOverskriftFeilutesendtVarselbrev(ytelseType)));
        task.setProperty("mottaker", dto.getMottaker().name());
        String taskId = prosessTaskRepository.lagre(task);
        logger.info("Opprettet task med id={} for utsending av fritekstbrev for behandlingId={}  til {}", taskId, dto.getBehandlingId(), dto.getMottaker());
        return Response.ok().build();
    }

    static String getTittelFeilutesendtVarselbrev() {
        return "Feilsendt varselbrev pga teknisk feil";
    }

    static String getOverskriftFeilutesendtVarselbrev(FagsakYtelseType ytelseType) {
        switch (ytelseType) {
            case ENGANGSTØNAD:
                return "Brev vedrørende feilutbetaling av engangsstønad";
            case FORELDREPENGER:
                return "Brev vedrørende feilutbetaling av foreldrepenger";
            case SVANGERSKAPSPENGER:
                return "Brev vedrørende feilutbetaling av svangerskapspenger";
            default:
                throw new IllegalArgumentException("Ikke-støttet ytelse-type: " + ytelseType);
        }
    }

    static String getInnholdFeilutsendtVarselbrev(FagsakYtelseType ytelseType) {
        String hilsen = "\n\nMed vennlig hilsen\nNAV Familie- og pensjonsytelser";
        switch (ytelseType) {
            case ENGANGSTØNAD:
                return "Det har på grunn av en teknisk feil blitt sendt brev til deg om at NAV vurderer om du må betale tilbake engangsstønad. Vi ber deg se bort fra dette brevet, datert 5. november 2020. Vi beklager feilen og ulempen dette har medført for deg." + hilsen;
            case FORELDREPENGER:
                return "Det har på grunn av en teknisk feil blitt sendt brev til deg om at NAV vurderer om du må betale tilbake foreldrepenger. Vi ber deg se bort fra dette brevet, datert 5. november 2020. Vi beklager feilen og ulempen dette har medført for deg." + hilsen;
            case SVANGERSKAPSPENGER:
                return "Det har på grunn av en teknisk feil blitt sendt brev til deg om at NAV vurderer om du må betale tilbake svangerskapspenger. Vi ber deg se bort fra dette brevet, datert 5. november 2020. Vi beklager feilen og ulempen dette har medført for deg." + hilsen;
            default:
                throw new IllegalArgumentException("Ikke-støttet ytelse-type: " + ytelseType);
        }
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
