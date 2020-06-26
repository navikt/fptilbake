package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.VENTEFRIST;

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
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingsTjenesteProvider;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingManglerKravgrunnlagFristenEndretEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingDtoTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingRettigheterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.ByttBehandlendeEnhetDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.FpsakUuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.GjenopptaBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.HenleggBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.KlageTilbakekrevingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.OpprettBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.ProsessTaskGruppeIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.Redirect;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.SettBehandlingPåVentDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UtvidetBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SøkestrengDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path(BehandlingRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
@Transactional
public class BehandlingRestTjeneste {
    public static final String PATH_FRAGMENT = "/behandlinger";

    private BehandlingTjeneste behandlingTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private BehandlingDtoTjeneste behandlingDtoTjeneste;
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private BehandlingRevurderingTjeneste revurderingTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private BehandlingManglerKravgrunnlagFristenEndretEventPubliserer fristenEndretEventPubliserer;

    public BehandlingRestTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingRestTjeneste(BehandlingsTjenesteProvider behandlingsTjenesteProvider,
                                  BehandlingDtoTjeneste behandlingDtoTjeneste,
                                  BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste,
                                  BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste,
                                  BehandlingManglerKravgrunnlagFristenEndretEventPubliserer fristenEndretEventPubliserer) {
        this.behandlingTjeneste = behandlingsTjenesteProvider.getBehandlingTjeneste();
        this.gjenopptaBehandlingTjeneste = behandlingsTjenesteProvider.getGjenopptaBehandlingTjeneste();
        this.behandlingDtoTjeneste = behandlingDtoTjeneste;
        this.behandlingsprosessTjeneste = behandlingsprosessTjeneste;
        this.henleggBehandlingTjeneste = behandlingsTjenesteProvider.getHenleggBehandlingTjeneste();
        this.revurderingTjeneste = behandlingsTjenesteProvider.getRevurderingTjeneste();
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
        this.behandlendeEnhetTjeneste = behandlingsTjenesteProvider.getEnhetTjeneste();
        this.fristenEndretEventPubliserer = fristenEndretEventPubliserer;
    }

    @GET
    @Operation(
        tags = "behandlinger",
        description = "Henter behandlinger knyttet til søkestreng",
        summary = "Ikke implementert enda")
    @Path("/finnbehandling")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response hentBehandling(@QueryParam("søkestring") @NotNull @Valid SøkestrengDto søkestreng) {
        // TODO (FM): implementer - skal akseptere saksnr, aktørId og FNR (?)
        return Response.noContent().build();
    }

    @POST
    @Operation(
        tags = "behandlinger",
        description = "Opprett ny behandling")
    @Path("/opprett")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response opprettBehandling(@Valid @NotNull OpprettBehandlingDto opprettBehandlingDto) throws URISyntaxException {
        Saksnummer saksnummer = new Saksnummer(opprettBehandlingDto.getSaksnummer());
        UUID eksternUuid = opprettBehandlingDto.getEksternUuid();
        BehandlingType behandlingType = opprettBehandlingDto.getBehandlingType();
        if (BehandlingType.TILBAKEKREVING.equals(behandlingType)) {
            Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternUuid, opprettBehandlingDto.getFagsakYtelseType(), behandlingType);
            return Redirect.tilBehandlingPollStatus(behandlingId, Optional.empty());
        } else if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandlingType)) {
            Long tbkBehandlingId = opprettBehandlingDto.getBehandlingId();
            Behandling revurdering = revurderingTjeneste.opprettRevurdering(tbkBehandlingId, opprettBehandlingDto.getBehandlingArsakType());
            String gruppe = behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(revurdering);
            return Redirect.tilBehandlingPollStatus(revurdering.getId(), Optional.of(gruppe));
        }
        return Response.ok().build();
    }

    @GET
    @Path("/kan-opprettes")
    @Operation(
        tags = "behandlinger",
        description = "Sjekk om behandling kan opprettes")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response kanOpprettesBehandling(@NotNull @QueryParam("saksnummer") @Valid SaksnummerDto saksnummerDto,
                                           @NotNull @QueryParam("uuid") @Valid FpsakUuidDto fpsakUuidDto) {
        Saksnummer saksnummer = new Saksnummer(saksnummerDto.getVerdi());
        UUID eksternUUID = fpsakUuidDto.getUuid();

        return Response.ok(behandlingTjeneste.kanOppretteBehandling(saksnummer, eksternUUID)).build();
    }

    @GET
    @Path("/kan-revurdering-opprettes")
    @Operation(
        tags = "behandlinger",
        description = "Sjekk om revurdering kan opprettes")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response kanOpprettesRevurdering(@NotNull @QueryParam("behandlingId") @Parameter(description = "Intern behandlingId eller behandlingUuid for behandling") @Valid BehandlingReferanse idDto) {
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
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public Response gjenopptaBehandling(@Parameter(description = "BehandlingId for behandling som skal gjenopptas") @Valid GjenopptaBehandlingDto dto)
        throws URISyntaxException {
        Long behandlingId = dto.getBehandlingId();

        // precondition - sjekk behandling versjon/lås
        behandlingTjeneste.kanEndreBehandling(behandlingId, dto.getBehandlingVersjon());

        // gjenoppta behandling
        Optional<String> gruppeOpt = gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandlingId);

        return Redirect.tilBehandlingPollStatus(behandlingId, gruppeOpt);
    }

    @POST
    @Timed
    @Path("/henlegg")
    @Operation(
        tags = "behandlinger",
        description = "Henlegger behandling")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public void henleggBehandling(@Parameter(description = "Henleggelse årsak") @Valid HenleggBehandlingDto dto) {
        Long behandlingId = dto.getBehandlingId();
        behandlingTjeneste.kanEndreBehandling(behandlingId, dto.getBehandlingVersjon());
        BehandlingResultatType årsakKode = tilHenleggBehandlingResultatType(dto.getÅrsakKode());
        henleggBehandlingTjeneste.henleggBehandlingManuelt(behandlingId, årsakKode, dto.getBegrunnelse());
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
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
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
    @BeskyttetRessurs(action = UPDATE, ressurs = VENTEFRIST)
    public void endreBehandlingPåVent(@Parameter(description = "Frist for behandling på vent") @Valid SettBehandlingPåVentDto dto) {
        behandlingTjeneste.kanEndreBehandling(dto.getBehandlingId(), dto.getBehandlingVersjon());
        behandlingTjeneste.endreBehandlingPåVent(dto.getBehandlingId(), dto.getFrist(), dto.getVentearsak());
        Behandling behandling = behandlingTjeneste.hentBehandling(dto.getBehandlingId());
        fristenEndretEventPubliserer.fireEvent(behandling, LocalDateTime.of(dto.getFrist(), LocalDateTime.now().toLocalTime()));
    }

    @GET
    @Path("/alle")
    @Timed
    @Operation(
        tags = "behandlinger",
        description = "Søk etter behandlinger på saksnummer", summary = "Returnerer alle behandlinger som er tilknyttet saksnummer.")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
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
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandling(@NotNull @Valid BehandlingReferanse idDto) throws URISyntaxException {
        // sender alltid til poll status slik at vi får sjekket på utestående prosess tasks også.
        return Redirect.tilBehandlingPollStatus(idDto.getBehandlingId(), Optional.empty());
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
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingMidlertidigStatus(@NotNull @QueryParam("behandlingId") @Valid BehandlingReferanse idDto,
                                                    @QueryParam("gruppe") @Valid ProsessTaskGruppeIdDto gruppeDto)
        throws URISyntaxException {
        Long behandlingId = idDto.getBehandlingId();
        String gruppe = gruppeDto == null ? null : gruppeDto.getGruppe();
        Behandling behandling = behandlingsprosessTjeneste.hentBehandling(behandlingId);
        Optional<AsyncPollingStatus> prosessTaskGruppePågår = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, gruppe);
        return Redirect.tilBehandlingEllerPollStatus(behandlingId, prosessTaskGruppePågår.orElse(null));
    }

    @GET
    @Timed
    @Operation(
        tags = "behandlinger",
        description = "Hent behandling gitt id", summary = "Returnerer behandlingen som er tilknyttet id. Dette er resultat etter at asynkrone operasjoner er utført.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returnerer behandling", content = @Content(schema = @Schema(implementation = UtvidetBehandlingDto.class)))
        })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingResultat(@NotNull @QueryParam("behandlingId") @Valid BehandlingReferanse idDto) {

        var behandlingId = idDto.getBehandlingId();
        var behandling = behandlingsprosessTjeneste.hentBehandling(behandlingId);

        AsyncPollingStatus taskStatus = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, null).orElse(null);

        UtvidetBehandlingDto dto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(idDto.getBehandlingId(), taskStatus);

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
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
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
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
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
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
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
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public BehandlingRettigheterDto hentBehandlingOperasjonRettigheter(
        @NotNull @QueryParam("behandlingUuid") @Valid BehandlingReferanse behandlingReferanse
    ) {
        Boolean harSoknad = true;
        //TODO (TOR) Denne skal etterkvart returnere rettighetene knytta til behandlingsmeny i frontend
        return new BehandlingRettigheterDto(harSoknad);
    }
}
