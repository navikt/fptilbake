package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.finn.FinnGrunnlagTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler.DokumentBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingDtoTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingOperasjonerDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingOpprettingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingRettigheterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.ByttBehandlendeEnhetDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.FpsakUuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.GjenopptaBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.HenleggBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.KlageTilbakekrevingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.OpprettBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.ProsessTaskGruppeIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.Redirect;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.SakFullDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.SakRettigheterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.SettBehandlingPåVentDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UtvidetBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SøkestrengDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.VergeBehandlingsmenyEnum;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.kontekst.Kontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@Path(BehandlingRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
@Transactional
public class BehandlingRestTjeneste {
    public static final String PATH_FRAGMENT = "/behandlinger";

    public static final String STATUS_PATH = PATH_FRAGMENT + "/status";

    private static final String SAK_FULL_PART_PATH = "/fagsak-full";
    public static final String SAK_FULL_PATH = PATH_FRAGMENT + SAK_FULL_PART_PATH;

    private static final String SAK_RETTIGHETER_PART_PATH = "/sak-rettigheter";
    public static final String SAK_RETTIGHETER_PATH = PATH_FRAGMENT + SAK_RETTIGHETER_PART_PATH;
    private static final String BEHANDLING_RETTIGHETER_PART_PATH = "/behandling-rettigheter";
    public static final String BEHANDLING_RETTIGHETER_PATH = PATH_FRAGMENT + BEHANDLING_RETTIGHETER_PART_PATH;

    private static final String BEHANDLING_ALLE_PART_PATH = "/alle";
    public static final String BEHANDLING_ALLE_PATH = PATH_FRAGMENT + BEHANDLING_ALLE_PART_PATH;

    private static final String BEHANDLING_KAN_OPPRETTES_PART_PATH = "/kan-opprettes";
    public static final String BEHANDLING_KAN_OPPRETTES_PATH = PATH_FRAGMENT + BEHANDLING_KAN_OPPRETTES_PART_PATH;
    private static final String REVURDERING_KAN_OPPRETTES_PART_PATH = "/kan-revurdering-opprettes-v2";
    public static final String REVURDERING_KAN_OPPRETTES_PATH = PATH_FRAGMENT + REVURDERING_KAN_OPPRETTES_PART_PATH;

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingDtoTjeneste behandlingDtoTjeneste;
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private BehandlingRevurderingTjeneste revurderingTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private TotrinnTjeneste totrinnTjeneste;
    private DokumentBehandlingTjeneste dokumentBehandlingTjeneste;
    private VergeTjeneste vergeTjeneste;
    private HistorikkTjenesteAdapter historikkTjenesteAdapter;

    public BehandlingRestTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingRestTjeneste(BehandlingTjeneste behandlingTjeneste,
                                  GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                                  BehandlingRevurderingTjeneste revurderingTjeneste,
                                  BehandlendeEnhetTjeneste enhetTjeneste,
                                  BehandlingDtoTjeneste behandlingDtoTjeneste,
                                  ProsessTaskTjeneste taskTjeneste,
                                  VergeTjeneste vergeTjeneste,
                                  TotrinnTjeneste totrinnTjeneste,
                                  DokumentBehandlingTjeneste dokumentBehandlingTjeneste,
                                  HenleggBehandlingTjeneste henleggBehandlingTjeneste,
                                  BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste,
                                  BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste,
                                  HistorikkTjenesteAdapter historikkTjenesteAdapter) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.behandlingDtoTjeneste = behandlingDtoTjeneste;
        this.vergeTjeneste = vergeTjeneste;
        this.behandlingsprosessTjeneste = behandlingsprosessTjeneste;
        this.henleggBehandlingTjeneste = henleggBehandlingTjeneste;
        this.revurderingTjeneste = revurderingTjeneste;
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
        this.behandlendeEnhetTjeneste = enhetTjeneste;
        this.taskTjeneste = taskTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
        this.dokumentBehandlingTjeneste = dokumentBehandlingTjeneste;
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
    }

    @GET
    @Operation(
            tags = "behandlinger",
            description = "Henter behandlinger knyttet til søkestreng",
            summary = "Ikke implementert enda")
    @Path("/finnbehandling")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response hentBehandling(@QueryParam("søkestring") @NotNull @Valid SøkestrengDto søkestreng) {
        // TODO (FM): implementer - skal akseptere saksnr, aktørId og FNR (?)
        return Response.noContent().build();
    }

    @POST
    @Operation(
            tags = "behandlinger",
            description = "Opprett ny behandling")
    @Path("/opprett")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response opprettBehandling(@Context HttpServletRequest request,
                                      @Valid @NotNull OpprettBehandlingDto opprettBehandlingDto) throws URISyntaxException {
        Saksnummer saksnummer = new Saksnummer(opprettBehandlingDto.getSaksnummer().getVerdi());
        UUID eksternUuid = opprettBehandlingDto.getEksternUuid();
        BehandlingType behandlingType = opprettBehandlingDto.getBehandlingType();
        if (BehandlingType.TILBAKEKREVING.equals(behandlingType)) {
            Long behandlingId = doOpprettBehandling(saksnummer, eksternUuid, opprettBehandlingDto.getFagsakYtelseType(), behandlingType);
            Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
            return Redirect.tilBehandlingPollStatus(request, behandling.getUuid(), Optional.empty());
        } else if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandlingType)) {
            Long tbkBehandlingId = opprettBehandlingDto.getBehandlingId() == null ? behandlingTjeneste.hentBehandlingId(opprettBehandlingDto.getBehandlingUuid())
                    : opprettBehandlingDto.getBehandlingId();
            var enhet = behandlingTjeneste.hentEnhetForEksternBehandling(opprettBehandlingDto.getEksternUuid());
            Behandling revurdering = revurderingTjeneste.opprettRevurdering(tbkBehandlingId, opprettBehandlingDto.getBehandlingArsakType(), enhet,
                Optional.ofNullable(KontekstHolder.getKontekst()).map(Kontekst::getUid).orElse(null));
            String gruppe = behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(revurdering);
            return Redirect.tilBehandlingPollStatus(request, revurdering.getUuid(), Optional.of(gruppe));
        }
        return Response.ok().build();
    }

    Long doOpprettBehandling(Saksnummer saksnummer, UUID eksternUuid, FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType) {
        var behandling = behandlingTjeneste.opprettKunBehandlingManuell(saksnummer, eksternUuid, fagsakYtelseType, behandlingType);
        var taskGruppe = new ProsessTaskGruppe();
        ProsessTaskData taskDataFortsett = ProsessTaskData.forProsessTask(FortsettBehandlingTask.class);
        taskDataFortsett.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskGruppe.addNesteSekvensiell(taskDataFortsett);
        ProsessTaskData taskDataFinn = ProsessTaskData.forProsessTask(FinnGrunnlagTask.class);
        taskDataFinn.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskGruppe.addNesteSekvensiell(taskDataFinn);
        taskGruppe.setCallIdFraEksisterende();
        taskTjeneste.lagre(taskGruppe);
        return behandling.getId();
    }

    @GET
    @Path(BEHANDLING_KAN_OPPRETTES_PART_PATH)
    @Operation(
            tags = "behandlinger",
            description = "Sjekk om behandling kan opprettes")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response kanOpprettesBehandling(@NotNull @QueryParam("saksnummer") @Valid SaksnummerDto saksnummerDto,
                                           @NotNull @QueryParam("uuid") @Valid FpsakUuidDto fpsakUuidDto) {
        Saksnummer saksnummer = new Saksnummer(saksnummerDto.getVerdi());
        UUID eksternUUID = fpsakUuidDto.getUuid();

        return Response.ok(behandlingTjeneste.kanOppretteBehandling(saksnummer, eksternUUID)).build();
    }

    // TODO: k9-tilbake. fjern når endringen er merget og prodsatt også i fpsak-frontend
    @GET
    @Path("/kan-revurdering-opprettes")
    @Operation(
            tags = "behandlinger",
            description = "Sjekk om revurdering kan opprettes")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response kanOpprettesRevurdering(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                            @NotNull @QueryParam("behandlingId") @Parameter(description = "Intern behandlingId eller behandlingUuid for behandling") @Valid BehandlingReferanse idDto) {
        return vurderOmRevurderingKanOpprettes(idDto);
    }

    @GET
    @Path(REVURDERING_KAN_OPPRETTES_PART_PATH)
    @Operation(
            tags = "behandlinger",
            description = "Sjekk om revurdering kan opprettes")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response kanRevurderingOpprettes(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                            @NotNull @QueryParam("uuid") @Parameter(description = "Intern behandlingId eller behandlingUuid for behandling") @Valid BehandlingReferanse idDto) {
        return vurderOmRevurderingKanOpprettes(idDto);
    }

    // TODO: k9-tilbake. refactor når endringen er merget og prodsatt også i fpsak-frontend
    private Response vurderOmRevurderingKanOpprettes(BehandlingReferanse idDto) {
        boolean kanRevurderingOprettes = false;
        if (idDto.erInternBehandlingId()) {
            Optional<EksternBehandling> eksternBehandling = revurderingTjeneste.hentEksternBehandling(idDto.getBehandlingId());
            if (eksternBehandling.isPresent()) {
                kanRevurderingOprettes = revurderingTjeneste.kanOppretteRevurdering(eksternBehandling.get().getEksternUuid());
            }
        } else {
            Behandling behandling = behandlingTjeneste.hentBehandling(idDto.getBehandlingUuid());
            kanRevurderingOprettes = behandling != null && revurderingTjeneste.kanRevurderingOpprettes(behandling);
        }
        return Response.ok(kanRevurderingOprettes).build();
    }

    @POST
    @Path("/gjenoppta")
    @Operation(
            tags = "behandlinger",
            description = "Gjenopptar behandling som er satt på vent",
            responses = {@ApiResponse(responseCode = "200", description = "Gjenoppta behandling påstartet i bakgrunnen", headers = {@Header(name = "Location")})})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, property = AbacProperty.FAGSAK)
    public Response gjenopptaBehandling(@Context HttpServletRequest request,
                                        @Parameter(description = "BehandlingId for behandling som skal gjenopptas") @Valid GjenopptaBehandlingDto dto)
            throws URISyntaxException {
        Behandling behandling = getBehandling(dto.getBehandlingReferanse());

        // precondition - sjekk behandling versjon/lås
        behandlingTjeneste.kanEndreBehandling(behandling.getId(), dto.getBehandlingVersjon());

        // gjenoppta behandling
        Optional<String> gruppeOpt = gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandling.getId(), HistorikkAktør.SAKSBEHANDLER,
            ResponsKanal.MANUELL);
        if (gruppeOpt.isPresent()) {
            behandlingTjeneste.setAnsvarligSaksbehandlerFraKontekst(behandling);
        }

        return Redirect.tilBehandlingPollStatus(request, behandling.getUuid(), gruppeOpt);
    }

    @POST
    @Path("/henlegg")
    @Operation(
            tags = "behandlinger",
            description = "Henlegger behandling")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, property = AbacProperty.FAGSAK)
    public void henleggBehandling(@Parameter(description = "Henleggelse årsak") @Valid HenleggBehandlingDto dto) {
        Long behandlingId = dto.getBehandlingId() == null ? behandlingTjeneste.hentBehandlingId(dto.getBehandlingUuid())
                : dto.getBehandlingId();
        behandlingTjeneste.kanEndreBehandling(behandlingId, dto.getBehandlingVersjon());
        BehandlingResultatType årsakKode = tilHenleggBehandlingResultatType(dto.getÅrsakKode());
        henleggBehandlingTjeneste.henleggBehandlingManuelt(behandlingId, årsakKode, dto.getBegrunnelse(), dto.getFritekst());
    }

    private BehandlingResultatType tilHenleggBehandlingResultatType(String årsak) {
        return BehandlingResultatType.getAlleHenleggelseskoder().stream().filter(k -> k.getKode().equals(årsak))
                .findFirst().orElse(null);
    }

    @POST
    @Path("/sett-pa-vent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "behandlinger",
            description = "Setter behandling på vent")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void settBehandlingPaVent(@Parameter(description = "Frist for behandling på vent") @Valid SettBehandlingPåVentDto dto) {
        var behandlingId = dto.getBehandlingId() == null ? behandlingTjeneste.hentBehandlingId(dto.getBehandlingUuid())
                : dto.getBehandlingId();
        behandlingTjeneste.kanEndreBehandling(behandlingId, dto.getBehandlingVersjon());
        behandlingTjeneste.settBehandlingPaVent(behandlingId, dto.getFrist(), dto.getVentearsak());
    }


    @POST
    @Path("/endre-pa-vent")
    @Operation(
            tags = "behandlinger",
            description = "Endrer ventefrist for behandling på vent")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, property = AbacProperty.VENTEFRIST)
    public void endreBehandlingPåVent(@Parameter(description = "Frist for behandling på vent") @Valid SettBehandlingPåVentDto dto) {
        var behandling = dto.getBehandlingUuid() == null ? behandlingTjeneste.hentBehandling(dto.getBehandlingId())
                : behandlingTjeneste.hentBehandling(dto.getBehandlingUuid());
        behandlingTjeneste.kanEndreBehandling(behandling.getId(), dto.getBehandlingVersjon());
        behandlingTjeneste.endreBehandlingPåVent(behandling, dto.getFrist(), dto.getVentearsak());
    }

    @GET
    @Path(BEHANDLING_ALLE_PART_PATH)
    @Operation(
            tags = "behandlinger",
            description = "Søk etter behandlinger på saksnummer", summary = "Returnerer alle behandlinger som er tilknyttet saksnummer.")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BehandlingDto> hentBehandlinger(
            @NotNull @QueryParam("saksnummer") @Parameter(description = "Saksnummer må være et eksisterende saksnummer") @Valid SaksnummerDto s) {
        Saksnummer saksnummer = new Saksnummer(s.getVerdi());
        return behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
    }

    @POST
    @Operation(
            tags = "behandlinger",
            description = "Init hent behandling",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Hent behandling initiert, Returnerer link til å polle på fremdrift", headers = {@Header(name = "Location")}),
                    @ApiResponse(responseCode = "303", description = "Behandling tilgjenglig (prosesstasks avsluttet)", headers = {@Header(name = "Location")})
            })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandling(@Context HttpServletRequest request,
                                   @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class) @NotNull @Valid BehandlingReferanse idDto) throws URISyntaxException {
        // sender alltid til poll status slik at vi får sjekket på utestående prosess tasks også.
        Behandling behandling = getBehandling(idDto);
        return Redirect.tilBehandlingPollStatus(request, behandling.getUuid(), Optional.empty());
    }

    @GET
    @Path("/status")
    @Operation(
            tags = "behandlinger",
            description = "Url for å polle på behandling mens behandlingprosessen pågår i bakgrunnen(asynkront)", summary = "Returnerer link til enten samme (hvis ikke ferdig) eller redirecter til /behandlinger dersom asynkrone operasjoner er ferdig.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returnerer Status", content = @Content(schema = @Schema(implementation = AsyncPollingStatus.class))),
                    @ApiResponse(responseCode = "418", description = "ProsessTasks har feilet", content = @Content(schema = @Schema(implementation = AsyncPollingStatus.class)), headers = {@Header(name = "Location")}),
                    @ApiResponse(responseCode = "303", description = "Behandling tilgjenglig (prosesstasks avsluttet)", content = @Content(schema = @Schema(implementation = AsyncPollingStatus.class)), headers = {@Header(name = "Location")}),
            })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingMidlertidigStatus(@Context HttpServletRequest request,
                                                    @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class) @NotNull @QueryParam("uuid") @Valid BehandlingReferanse idDto,
                                                    @QueryParam("gruppe") @Valid ProsessTaskGruppeIdDto gruppeDto)
            throws URISyntaxException {
        Behandling behandling = getBehandling(idDto);
        String gruppe = gruppeDto == null ? null : gruppeDto.getGruppe();
        Optional<AsyncPollingStatus> prosessTaskGruppePågår = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, gruppe);
        return Redirect.tilBehandlingEllerPollStatus(request, behandling.getUuid(), prosessTaskGruppePågår.orElse(null));
    }

    @GET
    @Operation(
            tags = "behandlinger",
            description = "Hent behandling gitt id", summary = "Returnerer behandlingen som er tilknyttet id. Dette er resultat etter at asynkrone operasjoner er utført.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returnerer behandling", content = @Content(schema = @Schema(implementation = UtvidetBehandlingDto.class)))
            })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingResultat(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                           @NotNull @QueryParam("uuid") @Valid BehandlingReferanse idDto) {
        var behandling = getBehandling(idDto);

        AsyncPollingStatus taskStatus = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, null).orElse(null);

        UtvidetBehandlingDto dto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(behandling.getId(), taskStatus);

        Response.ResponseBuilder responseBuilder = Response.ok().entity(dto);
        return responseBuilder.build();
    }

    //kun brukes av fpsak(backend)
    @GET
    @Path("/tilbakekreving/aapen")
    @Operation(
            tags = "behandlinger",
            description = "Sjekk hvis tilbakekrevingbehandling er åpen",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returnerer true hvis det finnes en åpen tilbakekrevingbehandling ellers false", content = @Content(schema = @Schema(implementation = Boolean.class)))
            })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response harÅpenTilbakekrevingBehandling(@NotNull @QueryParam("saksnummer") @Valid SaksnummerDto saksnummerDto) {
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(new Saksnummer(saksnummerDto.getVerdi()));
        boolean result = behandlinger.stream()
                .filter(behandling -> BehandlingType.TILBAKEKREVING.equals(behandling.getType()))
                .anyMatch(behandling -> !behandling.erAvsluttet());
        return Response.ok().entity(result).build();
    }

    //kun brukes av fpsak(backend)
    @GET
    @Path("/tilbakekreving/aapen-behandling")
    @Operation(
        tags = "behandlinger",
        description = "Sjekk om det finnes åpne behandlinger - tilbakekreving eller revurdering",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returnerer true hvis det finnes en åpen behandling ellers false", content = @Content(schema = @Schema(implementation = Boolean.class)))
        })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response harÅpenBehandling(@NotNull @QueryParam("saksnummer") @Valid SaksnummerDto saksnummerDto) {
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(new Saksnummer(saksnummerDto.getVerdi()));
        var result = behandlinger.stream().anyMatch(behandling -> !behandling.erAvsluttet());
        return Response.ok().entity(result).build();
    }

    //kun brukes av fpsak(backend)
    @GET
    @Path("/tilbakekreving/vedtak-info")
    @Operation(
            tags = "behandlinger",
            description = "Hent tilbakekrevingsvedtakInfo",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returnerer vedtak info for tilbakekreving", content = @Content(schema = @Schema(implementation = Boolean.class)))
            })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response hentTilbakekrevingsVedtakInfo(@NotNull @QueryParam(UuidDto.NAME) @Parameter(description = UuidDto.DESC) @Valid UuidDto uuidDto) {
        UUID behandlingUUId = uuidDto.getBehandlingUuid();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingUUId);
        Long behandlingId = behandling.getId();
        Optional<BehandlingVedtak> behandlingVedtak = behandlingTjeneste.hentBehandlingvedtakForBehandlingId(behandlingId);
        if (!behandling.erAvsluttet() || behandlingVedtak.isEmpty()) {
            throw BehandlingFeil.fantIkkeBehandlingsVedtakInfo(behandlingId);
        }
        KlageTilbakekrevingDto klageTilbakekrevingDto = new KlageTilbakekrevingDto(behandlingId, behandlingVedtak.get().getVedtaksdato(), behandling.getType().getKode());
        return Response.ok().entity(klageTilbakekrevingDto).build();
    }

    @POST
    @Path("/bytt-enhet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "behandlinger",
            description = "Bytte behandlende enhet")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void byttBehandlendeEnhet(@Parameter(description = "Ny enhet som skal byttes") @Valid ByttBehandlendeEnhetDto dto) {
        Long behandlingId = dto.getBehandlingId() == null ? behandlingTjeneste.hentBehandlingId(dto.getBehandlingUuid())
                : dto.getBehandlingId();
        Long behandlingVersjon = dto.getBehandlingVersjon();
        behandlingTjeneste.kanEndreBehandling(behandlingId, behandlingVersjon);

        String enhetId = dto.getEnhetId();
        String enhetNavn = dto.getEnhetNavn();
        behandlendeEnhetTjeneste.byttBehandlendeEnhet(behandlingId, new OrganisasjonsEnhet(enhetId, enhetNavn), HistorikkAktør.SAKSBEHANDLER);
    }

    @GET
    @Path("/handling-rettigheter")
    @Operation(
            tags = "behandlinger",
            description = "Henter rettigheter for lovlige behandlingsoperasjoner")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public BehandlingRettigheterDto hentBehandlingOperasjonRettigheter(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                                                       @NotNull @QueryParam("uuid") @Valid BehandlingReferanse behandlingReferanse
    ) {
        Boolean harSoknad = true;
        //TODO (TOR) Denne skal etterkvart returnere rettighetene knytta til behandlingsmeny i frontend
        return new BehandlingRettigheterDto(harSoknad);
    }

    @GET
    @Path(SAK_RETTIGHETER_PART_PATH)
    @Operation(
            tags = "behandlinger",
            description = "Henter rettigheter for lovlige behandlingsoprettinger for sak")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public SakRettigheterDto hentRettigheterSak(@NotNull @QueryParam("saksnummer") @Valid SaksnummerDto saksnummerDto) {
        Saksnummer saksnummer = new Saksnummer(saksnummerDto.getVerdi());
        var kanOppretteTilbake = behandlingTjeneste.hentBehandlinger(saksnummer).stream().allMatch(Behandling::erSaksbehandlingAvsluttet);
        var kanOppretteRevurdering = behandlingTjeneste.hentBehandlinger(saksnummer).stream().anyMatch(revurderingTjeneste::kanRevurderingOpprettes);
        var oppretting = List.of(new BehandlingOpprettingDto(BehandlingType.TILBAKEKREVING, kanOppretteTilbake),
                new BehandlingOpprettingDto(BehandlingType.REVURDERING_TILBAKEKREVING, kanOppretteRevurdering));
        return new SakRettigheterDto(false, oppretting, List.of());
    }


    // TBK opprette når kommer med yelsebehandling UUID return !(harÅpenBehandling(saksnummer) || finnesTilbakekrevingsbehandlingForYtelsesbehandlingen(eksternUuid));
    // TBK revurdering: gitt uuid - vurderOmRevurderingKanOpprettes
    @GET
    @Path(SAK_FULL_PART_PATH)
    @Operation(
        tags = "behandlinger",
        description = "Henter informasjon om rettigheter, behandlinger og historikk for sak")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public SakFullDto hentSaksinformasjon(@Context HttpServletRequest request, @NotNull @QueryParam("saksnummer") @Valid SaksnummerDto saksnummerDto) {
        Saksnummer saksnummer = new Saksnummer(saksnummerDto.getVerdi());
        var hentDokumentPath = historikkTjenesteAdapter.getRequestPath(request);
        var historikkInnslagDtoList = historikkTjenesteAdapter.hentAlleHistorikkInnslagForSak(new Saksnummer(saksnummerDto.getVerdi()), hentDokumentPath);
        var kanOppretteTilbake = behandlingTjeneste.hentBehandlinger(saksnummer).stream().allMatch(Behandling::erSaksbehandlingAvsluttet);
        var kanOppretteRevurdering = behandlingTjeneste.hentBehandlinger(saksnummer).stream().anyMatch(revurderingTjeneste::kanRevurderingOpprettes);
        var oppretting = List.of(new BehandlingOpprettingDto(BehandlingType.TILBAKEKREVING, kanOppretteTilbake),
            new BehandlingOpprettingDto(BehandlingType.REVURDERING_TILBAKEKREVING, kanOppretteRevurdering));
        var behandlinger = behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
        behandlinger.forEach(b -> b.setBrevmaler(dokumentBehandlingTjeneste.hentBrevmalerFor(b.getId())));
        return new SakFullDto(saksnummer.getVerdi(), oppretting, behandlinger, historikkInnslagDtoList);
    }

    @GET
    @Path(BEHANDLING_RETTIGHETER_PART_PATH)
    @Operation(
            tags = "behandlinger",
            description = "Henter rettigheter for lovlige behandlingsoperasjoner")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public BehandlingOperasjonerDto hentMenyOpsjoner(@NotNull @QueryParam(UuidDto.NAME) @Parameter(description = UuidDto.DESC) @Valid UuidDto uuidDto) {
        UUID behandlingUUId = uuidDto.getBehandlingUuid();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingUUId);
        return lovligeOperasjoner(behandling, vergeTjeneste.hentVergeInformasjon(behandling.getId()).isPresent());
    }

    private Behandling getBehandling(BehandlingReferanse behandlingReferanse) {
        Behandling behandling;
        if (behandlingReferanse.erInternBehandlingId()) {
            behandling = behandlingTjeneste.hentBehandling(behandlingReferanse.getBehandlingId());
        } else {
            behandling = behandlingTjeneste.hentBehandling(behandlingReferanse.getBehandlingUuid());
        }
        return behandling;
    }

    private BehandlingOperasjonerDto lovligeOperasjoner(Behandling b, boolean finnesVerge) {
        if (b.erSaksbehandlingAvsluttet()) {
            return BehandlingOperasjonerDto.builder(b.getUuid()).build(); // Skal ikke foreta menyvalg lenger
        } else if (BehandlingStatus.FATTER_VEDTAK.equals(b.getStatus())) {
            boolean tilgokjenning = b.getAnsvarligSaksbehandler() != null &&
                !b.getAnsvarligSaksbehandler().equalsIgnoreCase(KontekstHolder.getKontekst().getUid());
            return BehandlingOperasjonerDto.builder(b.getUuid()).medTilGodkjenning(tilgokjenning).build();
        } else {
            boolean totrinnRetur = totrinnTjeneste.hentTotrinnsvurderinger(b).stream().anyMatch(tt -> !tt.isGodkjent());
            return BehandlingOperasjonerDto.builder(b.getUuid())
                .medTilGodkjenning(false)
                .medFraBeslutter(!b.isBehandlingPåVent() && totrinnRetur)
                .medKanBytteEnhet(true)
                .medKanHenlegges(henleggBehandlingTjeneste.kanHenleggeBehandlingManuelt(b))
                .medKanSettesPaVent(!b.isBehandlingPåVent())
                .medKanGjenopptas(b.isBehandlingPåVent())
                .medKanOpnesForEndringer(false)
                .medKanSendeMelding(!b.isBehandlingPåVent())
                .medVergemeny(viseVerge(b, finnesVerge))
                .build();
        }
    }

    private VergeBehandlingsmenyEnum viseVerge(Behandling behandling, boolean finnesVerge) {
        boolean kanBehandlingEndres = !behandling.erSaksbehandlingAvsluttet() && !behandling.isBehandlingPåVent();
        if (kanBehandlingEndres) {
            return finnesVerge ? VergeBehandlingsmenyEnum.FJERN : VergeBehandlingsmenyEnum.OPPRETT;
        }
        return VergeBehandlingsmenyEnum.SKJUL;
    }


}
