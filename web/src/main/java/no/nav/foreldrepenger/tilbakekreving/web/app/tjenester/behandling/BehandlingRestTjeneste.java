package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import com.codahale.metrics.annotation.Timed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingsTjenesteProvider;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingManglerKravgrunnlagFristenEndretEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
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
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.SakRettigheterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.SettBehandlingPåVentDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UtvidetBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SøkestrengDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.VergeBehandlingsmenyEnum;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.util.env.Environment;

@Path(BehandlingRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
@Transactional
public class BehandlingRestTjeneste {
    public static final String PATH_FRAGMENT = "/behandlinger";

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


    private BehandlingTjeneste behandlingTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingDtoTjeneste behandlingDtoTjeneste;
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private BehandlingRevurderingTjeneste revurderingTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private BehandlingManglerKravgrunnlagFristenEndretEventPubliserer fristenEndretEventPubliserer;
    private TotrinnTjeneste totrinnTjeneste;
    private VergeTjeneste vergeTjeneste;

    public BehandlingRestTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingRestTjeneste(BehandlingsTjenesteProvider behandlingsTjenesteProvider,
                                  BehandlingDtoTjeneste behandlingDtoTjeneste,
                                  VergeTjeneste vergeTjeneste,
                                  TotrinnTjeneste totrinnTjeneste,
                                  BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste,
                                  BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste,
                                  BehandlingManglerKravgrunnlagFristenEndretEventPubliserer fristenEndretEventPubliserer) {
        this.behandlingTjeneste = behandlingsTjenesteProvider.getBehandlingTjeneste();
        this.gjenopptaBehandlingTjeneste = behandlingsTjenesteProvider.getGjenopptaBehandlingTjeneste();
        this.behandlingDtoTjeneste = behandlingDtoTjeneste;
        this.vergeTjeneste = vergeTjeneste;
        this.behandlingsprosessTjeneste = behandlingsprosessTjeneste;
        this.henleggBehandlingTjeneste = behandlingsTjenesteProvider.getHenleggBehandlingTjeneste();
        this.revurderingTjeneste = behandlingsTjenesteProvider.getRevurderingTjeneste();
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
        this.behandlendeEnhetTjeneste = behandlingsTjenesteProvider.getEnhetTjeneste();
        this.fristenEndretEventPubliserer = fristenEndretEventPubliserer;
        this.vergeTjeneste = vergeTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
    }

    @GET
    @Operation(
        tags = "behandlinger",
        description = "Henter behandlinger knyttet til søkestreng",
        summary = "Ikke implementert enda")
    @Path("/finnbehandling")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public Response hentBehandling(@QueryParam("søkestring") @NotNull @Valid SøkestrengDto søkestreng) {
        // TODO (FM): implementer - skal akseptere saksnr, aktørId og FNR (?)
        return Response.noContent().build();
    }

    @POST
    @Operation(
        tags = "behandlinger",
        description = "Opprett ny behandling")
    @Path("/opprett")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public Response opprettBehandling(@Valid @NotNull OpprettBehandlingDto opprettBehandlingDto) throws URISyntaxException {
        Saksnummer saksnummer = new Saksnummer(opprettBehandlingDto.getSaksnummer().getVerdi());
        UUID eksternUuid = opprettBehandlingDto.getEksternUuid();
        BehandlingType behandlingType = opprettBehandlingDto.getBehandlingType();
        if (BehandlingType.TILBAKEKREVING.equals(behandlingType)) {
            Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternUuid, opprettBehandlingDto.getFagsakYtelseType(), behandlingType);
            Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
            return Redirect.tilBehandlingPollStatus(behandling.getUuid(), Optional.empty());
        } else if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandlingType)) {
            validerAktivertFunksjonalitetForRevurdering(opprettBehandlingDto);
            Long tbkBehandlingId = opprettBehandlingDto.getBehandlingId();
            Behandling revurdering = revurderingTjeneste.opprettRevurdering(tbkBehandlingId, opprettBehandlingDto.getBehandlingArsakType());
            String gruppe = behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(revurdering);
            return Redirect.tilBehandlingPollStatus(revurdering.getUuid(), Optional.of(gruppe));
        }
        return Response.ok().build();
    }

    static void validerAktivertFunksjonalitetForRevurdering(OpprettBehandlingDto dto) {
        if (Environment.current().isProd()
            && dto.getBehandlingType() == BehandlingType.REVURDERING_TILBAKEKREVING
            && dto.getBehandlingArsakType() == BehandlingÅrsakType.RE_FEILUTBETALT_BELØP_HELT_ELLER_DELVIS_BORTFALT) {
            throw new IllegalArgumentException("Behandlingsårsaken 'Feilutbetalt beløp helt eller delvis bortfalt' er ikke lansert enda. Forvent at funksjonaliteten kan tas i bruk i løpet av uke 45 i 2020.");
        }
    }

    @GET
    @Path(BEHANDLING_KAN_OPPRETTES_PART_PATH)
    @Operation(
        tags = "behandlinger",
        description = "Sjekk om behandling kan opprettes")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
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
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public Response kanOpprettesRevurdering(@NotNull @QueryParam("behandlingId") @Parameter(description = "Intern behandlingId eller behandlingUuid for behandling") @Valid BehandlingReferanse idDto) {
        return vurderOmRevurderingKanOpprettes(idDto);
    }

    @GET
    @Path(REVURDERING_KAN_OPPRETTES_PART_PATH)
    @Operation(
        tags = "behandlinger",
        description = "Sjekk om revurdering kan opprettes")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public Response kanRevurderingOpprettes(@NotNull @QueryParam("uuid") @Parameter(description = "Intern behandlingId eller behandlingUuid for behandling") @Valid BehandlingReferanse idDto) {
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
    @Timed
    @Path("/gjenoppta")
    @Operation(
        tags = "behandlinger",
        description = "Gjenopptar behandling som er satt på vent",
        responses = {@ApiResponse(responseCode = "200", description = "Gjenoppta behandling påstartet i bakgrunnen", headers = {@Header(name = "Location")})})
    @BeskyttetRessurs(action = UPDATE, property = AbacProperty.FAGSAK)
    public Response gjenopptaBehandling(@Parameter(description = "BehandlingId for behandling som skal gjenopptas") @Valid GjenopptaBehandlingDto dto)
        throws URISyntaxException {
        Behandling behandling = getBehandling(dto.getBehandlingReferanse());

        // precondition - sjekk behandling versjon/lås
        behandlingTjeneste.kanEndreBehandling(behandling.getId(), dto.getBehandlingVersjon());

        // gjenoppta behandling
        Optional<String> gruppeOpt = gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandling.getId());

        return Redirect.tilBehandlingPollStatus(behandling.getUuid(), gruppeOpt);
    }

    @POST
    @Timed
    @Path("/henlegg")
    @Operation(
        tags = "behandlinger",
        description = "Henlegger behandling")
    @BeskyttetRessurs(action = UPDATE, property = AbacProperty.FAGSAK)
    public void henleggBehandling(@Parameter(description = "Henleggelse årsak") @Valid HenleggBehandlingDto dto) {
        Long behandlingId = dto.getBehandlingId();
        behandlingTjeneste.kanEndreBehandling(behandlingId, dto.getBehandlingVersjon());
        BehandlingResultatType årsakKode = tilHenleggBehandlingResultatType(dto.getÅrsakKode());
        henleggBehandlingTjeneste.henleggBehandlingManuelt(behandlingId, årsakKode, dto.getBegrunnelse(), dto.getFritekst());
    }

    private BehandlingResultatType tilHenleggBehandlingResultatType(String årsak) {
        return BehandlingResultatType.getAlleHenleggelseskoder().stream().filter(k -> k.getKode().equals(årsak))
            .findFirst().orElse(null);
    }

    @POST
    @Timed
    @Path("/sett-pa-vent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        tags = "behandlinger",
        description = "Setter behandling på vent")
    @BeskyttetRessurs(action = UPDATE, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void settBehandlingPaVent(@Parameter(description = "Frist for behandling på vent") @Valid SettBehandlingPåVentDto dto) {
        behandlingTjeneste.kanEndreBehandling(dto.getBehandlingId(), dto.getBehandlingVersjon());
        behandlingTjeneste.settBehandlingPaVent(dto.getBehandlingId(), dto.getFrist(), dto.getVentearsak());
    }


    @POST
    @Timed
    @Path("/endre-pa-vent")
    @Operation(
        tags = "behandlinger",
        description = "Endrer ventefrist for behandling på vent")
    @BeskyttetRessurs(action = UPDATE, property = AbacProperty.VENTEFRIST)
    public void endreBehandlingPåVent(@Parameter(description = "Frist for behandling på vent") @Valid SettBehandlingPåVentDto dto) {
        behandlingTjeneste.kanEndreBehandling(dto.getBehandlingId(), dto.getBehandlingVersjon());
        behandlingTjeneste.endreBehandlingPåVent(dto.getBehandlingId(), dto.getFrist(), dto.getVentearsak());
        Behandling behandling = behandlingTjeneste.hentBehandling(dto.getBehandlingId());
        fristenEndretEventPubliserer.fireEvent(behandling, LocalDateTime.of(dto.getFrist(), LocalDateTime.now().toLocalTime()));
    }

    @GET
    @Path(BEHANDLING_ALLE_PART_PATH)
    @Timed
    @Operation(
        tags = "behandlinger",
        description = "Søk etter behandlinger på saksnummer", summary = "Returnerer alle behandlinger som er tilknyttet saksnummer.")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BehandlingDto> hentBehandlinger(
        @NotNull @QueryParam("saksnummer") @Parameter(description = "Saksnummer må være et eksisterende saksnummer") @Valid SaksnummerDto s) {
        Saksnummer saksnummer = new Saksnummer(s.getVerdi());
        return behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
    }

    @POST
    @Timed
    @Operation(
        tags = "behandlinger",
        description = "Init hent behandling",
        responses = {
            @ApiResponse(responseCode = "202", description = "Hent behandling initiert, Returnerer link til å polle på fremdrift", headers = {@Header(name = "Location")}),
            @ApiResponse(responseCode = "303", description = "Behandling tilgjenglig (prosesstasks avsluttet)", headers = {@Header(name = "Location")})
        })
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandling(@NotNull @Valid BehandlingReferanse idDto) throws URISyntaxException {
        // sender alltid til poll status slik at vi får sjekket på utestående prosess tasks også.
        Behandling behandling = getBehandling(idDto);
        return Redirect.tilBehandlingPollStatus(behandling.getUuid(), Optional.empty());
    }

    @GET
    @Path("/status")
    @Timed
    @Operation(
        tags = "behandlinger",
        description = "Url for å polle på behandling mens behandlingprosessen pågår i bakgrunnen(asynkront)", summary = "Returnerer link til enten samme (hvis ikke ferdig) eller redirecter til /behandlinger dersom asynkrone operasjoner er ferdig.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returnerer Status", content = @Content(schema = @Schema(implementation = AsyncPollingStatus.class))),
            @ApiResponse(responseCode = "418", description = "ProsessTasks har feilet", content = @Content(schema = @Schema(implementation = AsyncPollingStatus.class)), headers = {@Header(name = "Location")}),
            @ApiResponse(responseCode = "303", description = "Behandling tilgjenglig (prosesstasks avsluttet)", content = @Content(schema = @Schema(implementation = AsyncPollingStatus.class)), headers = {@Header(name = "Location")}),
        })
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingMidlertidigStatus(@NotNull @QueryParam("uuid") @Valid BehandlingReferanse idDto,
                                                    @QueryParam("gruppe") @Valid ProsessTaskGruppeIdDto gruppeDto)
        throws URISyntaxException {
        Behandling behandling = getBehandling(idDto);
        String gruppe = gruppeDto == null ? null : gruppeDto.getGruppe();
        Optional<AsyncPollingStatus> prosessTaskGruppePågår = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, gruppe);
        return Redirect.tilBehandlingEllerPollStatus(behandling.getUuid(), prosessTaskGruppePågår.orElse(null));
    }

    @GET
    @Timed
    @Operation(
        tags = "behandlinger",
        description = "Hent behandling gitt id", summary = "Returnerer behandlingen som er tilknyttet id. Dette er resultat etter at asynkrone operasjoner er utført.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returnerer behandling", content = @Content(schema = @Schema(implementation = UtvidetBehandlingDto.class)))
        })
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingResultat(@NotNull @QueryParam("uuid") @Valid BehandlingReferanse idDto) {
        var behandling = getBehandling(idDto);

        AsyncPollingStatus taskStatus = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, null).orElse(null);

        UtvidetBehandlingDto dto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(behandling.getId(), taskStatus);

        Response.ResponseBuilder responseBuilder = Response.ok().entity(dto);
        return responseBuilder.build();
    }

    //kun brukes av fpsak(backend)
    @GET
    @Path("/tilbakekreving/aapen")
    @Timed
    @Operation(
        tags = "behandlinger",
        description = "Sjekk hvis tilbakekrevingbehandling er åpen",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returnerer true hvis det finnes en åpen tilbakekrevingbehandling ellers false", content = @Content(schema = @Schema(implementation = Boolean.class)))
        })
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public Response harÅpenTilbakekrevingBehandling(@NotNull @QueryParam("saksnummer") @Valid SaksnummerDto saksnummerDto) {
        List<Behandling> behandlinger = behandlingTjeneste.hentBehandlinger(new Saksnummer(saksnummerDto.getVerdi()));
        boolean result = behandlinger.stream()
            .filter(behandling -> BehandlingType.TILBAKEKREVING.equals(behandling.getType()))
            .anyMatch(behandling -> !behandling.erAvsluttet());
        return Response.ok().entity(result).build();
    }

    //kun brukes av fpsak(backend)
    @GET
    @Path("/tilbakekreving/vedtak-info")
    @Timed
    @Operation(
        tags = "behandlinger",
        description = "Hent tilbakekrevingsvedtakInfo",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returnerer vedtak info for tilbakekreving", content = @Content(schema = @Schema(implementation = Boolean.class)))
        })
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public Response hentTilbakekrevingsVedtakInfo(@NotNull @QueryParam(UuidDto.NAME) @Parameter(description = UuidDto.DESC) @Valid UuidDto uuidDto) {
        UUID behandlingUUId = uuidDto.getBehandlingUuid();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingUUId);
        Long behandlingId = behandling.getId();
        Optional<BehandlingVedtak> behandlingVedtak = behandlingTjeneste.hentBehandlingvedtakForBehandlingId(behandlingId);
        if (!behandling.erAvsluttet() || behandlingVedtak.isEmpty()) {
            throw BehandlingFeil.FACTORY.fantIkkeBehandlingsVedtakInfo(behandlingId).toException();
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
    @BeskyttetRessurs(action = UPDATE, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void byttBehandlendeEnhet(@Parameter(description = "Ny enhet som skal byttes") @Valid ByttBehandlendeEnhetDto dto) {
        Long behandlingId = dto.getBehandlingId();
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
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public BehandlingRettigheterDto hentBehandlingOperasjonRettigheter(
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
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public SakRettigheterDto hentRettigheterSak(@NotNull @QueryParam("saksnummer") @Valid SaksnummerDto saksnummerDto) {
        Saksnummer saksnummer = new Saksnummer(saksnummerDto.getVerdi());
        var kanOppretteTilbake = behandlingTjeneste.hentBehandlinger(saksnummer).stream().allMatch(Behandling::erSaksbehandlingAvsluttet);
        var kanOppretteRevurdering = behandlingTjeneste.hentBehandlinger(saksnummer).stream().anyMatch(revurderingTjeneste::kanRevurderingOpprettes);
        var oppretting = List.of(new BehandlingOpprettingDto(BehandlingType.TILBAKEKREVING, kanOppretteTilbake),
            new BehandlingOpprettingDto(BehandlingType.REVURDERING_TILBAKEKREVING, kanOppretteRevurdering));
        return new SakRettigheterDto(false, oppretting, List.of());
    }

    @GET
    @Path(BEHANDLING_RETTIGHETER_PART_PATH)
    @Operation(
        tags = "behandlinger",
        description = "Henter rettigheter for lovlige behandlingsoperasjoner")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public BehandlingOperasjonerDto hentMenyOpsjoner(@NotNull @QueryParam(UuidDto.NAME) @Parameter(description = UuidDto.DESC) @Valid UuidDto uuidDto) {
        UUID behandlingUUId = uuidDto.getBehandlingUuid();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingUUId);
        return lovligeOperasjoner(behandling);
    }

    private VergeBehandlingsmenyEnum viseVerge(Behandling behandling) {
        boolean kanBehandlingEndres = !behandling.erSaksbehandlingAvsluttet() && !behandling.isBehandlingPåVent();
        boolean finnesVerge = vergeTjeneste.hentVergeInformasjon(behandling.getId()).isPresent();
        if (kanBehandlingEndres) {
            return finnesVerge ? VergeBehandlingsmenyEnum.FJERN : VergeBehandlingsmenyEnum.OPPRETT;
        }
        return VergeBehandlingsmenyEnum.SKJUL;
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

    private BehandlingOperasjonerDto lovligeOperasjoner(Behandling b) {
        if (b.erSaksbehandlingAvsluttet()) {
            return BehandlingOperasjonerDto.builder(b.getUuid()).build(); // Skal ikke foreta menyvalg lenger
        } else if (BehandlingStatus.FATTER_VEDTAK.equals(b.getStatus())) {
            boolean tilgokjenning = b.getAnsvarligSaksbehandler() != null && !b.getAnsvarligSaksbehandler().equalsIgnoreCase(SubjectHandler.getSubjectHandler().getUid());
            return BehandlingOperasjonerDto.builder(b.getUuid()).medTilGodkjenning(tilgokjenning).build();
        } else {
            boolean totrinnRetur = totrinnTjeneste.hentTotrinnsvurderinger(b).stream().anyMatch(tt -> !tt.isGodkjent());
            return BehandlingOperasjonerDto.builder(b.getUuid())
                .medTilGodkjenning(false)
                .medFraBeslutter(!b.isBehandlingPåVent() && totrinnRetur)
                .medKanBytteEnhet(true)
                .medKanHenlegges(henleggBehandlingTjeneste.kanHenleggeBehandlingManuelt(b.getId(), b.getType()))
                .medKanSettesPaVent(!b.isBehandlingPåVent())
                .medKanGjenopptas(b.isBehandlingPåVent())
                .medKanOpnesForEndringer(false)
                .medKanSendeMelding(!b.isBehandlingPåVent())
                .medVergemeny(viseVerge(b))
                .build();
        }
    }
}
