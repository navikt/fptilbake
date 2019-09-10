package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.VENTEFRIST;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;
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

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingsTjenesteProvider;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingDtoTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.ByttBehandlendeEnhetDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.GjenopptaBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.HenleggBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.OpprettBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.ProsessTaskGruppeIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.Redirect;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.SettBehandlingPåVentDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UtvidetBehandlingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SøkestrengDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "behandlinger")
@Path(BehandlingRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
@Transaction
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

    public BehandlingRestTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingRestTjeneste(BehandlingsTjenesteProvider behandlingsTjenesteProvider,
                                  BehandlingDtoTjeneste behandlingDtoTjeneste,
                                  BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste,
                                  BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste) {
        this.behandlingTjeneste = behandlingsTjenesteProvider.getBehandlingTjeneste();
        this.gjenopptaBehandlingTjeneste = behandlingsTjenesteProvider.getGjenopptaBehandlingTjeneste();
        this.behandlingDtoTjeneste = behandlingDtoTjeneste;
        this.behandlingsprosessTjeneste = behandlingsprosessTjeneste;
        this.henleggBehandlingTjeneste = behandlingsTjenesteProvider.getHenleggBehandlingTjeneste();
        this.revurderingTjeneste = behandlingsTjenesteProvider.getRevurderingTjeneste();
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
        this.behandlendeEnhetTjeneste = behandlingsTjenesteProvider.getEnhetTjeneste();
    }

    @GET
    @ApiOperation(value = "Henter behandlinger knyttet til søkestreng", notes = "Ikke implementert enda")
    @ApiResponses(value = {

    })
    @Path("/finnbehandling")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response hentBehandling(@QueryParam("søkestring") @NotNull @Valid SøkestrengDto søkestreng) {
        // TODO (FM): implementer - skal akseptere saksnr, aktørId og FNR (?)
        return Response.noContent().build();
    }

    @POST
    @ApiOperation(value = "Opprett ny behandling")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")
    })
    @Path("/opprett")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response opprettBehandling(@Valid @NotNull OpprettBehandlingDto opprettBehandlingDto) throws URISyntaxException {
        Saksnummer saksnummer = new Saksnummer(opprettBehandlingDto.getSaksnummer());
        UUID eksternUuid = opprettBehandlingDto.getEksternUuid();
        AktørId aktørId = new AktørId(opprettBehandlingDto.getAktørId());
        BehandlingType behandlingType = BehandlingType.fraKode(opprettBehandlingDto.getBehandlingType());
        if (BehandlingType.TILBAKEKREVING.equals(behandlingType)) {
            behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternUuid, aktørId, opprettBehandlingDto.getFagsakYtelseType(), behandlingType);
            return Redirect.tilFagsakPollStatus(saksnummer, Optional.empty());
        } else if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandlingType)) {
            Behandling revurdering = revurderingTjeneste.opprettRevurdering(saksnummer, eksternUuid, opprettBehandlingDto.getBehandlingArsakType());
            String gruppe = behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(revurdering);
            return Redirect.tilBehandlingPollStatus(revurdering.getId(), Optional.of(gruppe));
        }
        return Response.ok().build();
    }

    @GET
    @Path("/revurdering/kan-opprettes")
    @ApiOperation(value = "Sjekk om revurdering kan opprettes")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response kanOppretteRevurdering(@QueryParam("uuid") @NotNull @Valid UuidDto uuidDto) {
        boolean result = revurderingTjeneste.kanOppretteRevurdering(uuidDto.getUuid());
        return Response.ok(result).build();
    }

    @POST
    @Timed
    @Path("/gjenoppta")
    @ApiOperation(value = "Gjenopptar behandling som er satt på vent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Gjenoppta behandling påstartet i bakgrunnen", responseHeaders = {@ResponseHeader(name = "Location")})
    })
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public Response gjenopptaBehandling(@ApiParam("BehandlingId for behandling som skal gjenopptas") @Valid GjenopptaBehandlingDto dto)
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
    @ApiOperation(value = "Henlegger behandling")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public void henleggBehandling(@ApiParam("Henleggelse årsak") @Valid HenleggBehandlingDto dto) {
        Long behandlingId = dto.getBehandlingId();
        behandlingTjeneste.kanEndreBehandling(behandlingId, dto.getBehandlingVersjon());
        BehandlingResultatType årsakKode = tilHenleggBehandlingResultatType(dto.getÅrsakKode());
        henleggBehandlingTjeneste.henleggBehandling(behandlingId, årsakKode, dto.getBegrunnelse());
    }

    private BehandlingResultatType tilHenleggBehandlingResultatType(String årsak) {
        return BehandlingResultatType.getAlleHenleggelseskoder().stream().filter(k -> k.getKode().equals(årsak))
            .findFirst().orElse(null);
    }

    @POST
    @Timed
    @Path("/sett-pa-vent")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Setter behandling på vent")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void settBehandlingPaVent(@ApiParam("Frist for behandling på vent") @Valid SettBehandlingPåVentDto dto) {
        behandlingTjeneste.kanEndreBehandling(dto.getBehandlingId(), dto.getBehandlingVersjon());
        behandlingTjeneste.settBehandlingPaVent(dto.getBehandlingId(), dto.getFrist(), dto.getVentearsak());
    }


    @POST
    @Timed
    @Path("/endre-pa-vent")
    @ApiOperation(value = "Endrer ventefrist for behandling på vent")
    @BeskyttetRessurs(action = UPDATE, ressurs = VENTEFRIST)
    public void endreBehandlingPåVent(@ApiParam("Frist for behandling på vent") @Valid SettBehandlingPåVentDto dto) {
        behandlingTjeneste.kanEndreBehandling(dto.getBehandlingId(), dto.getBehandlingVersjon());
        behandlingTjeneste.endreBehandlingPåVent(dto.getBehandlingId(), dto.getFrist(), dto.getVentearsak());
    }

    @GET
    @Path("/alle")
    @Timed
    @ApiOperation(value = "Søk etter behandlinger på saksnummer", notes = ("Returnerer alle behandlinger som er tilknyttet saksnummer."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BehandlingDto> hentBehandlinger(
        @NotNull @QueryParam("saksnummer") @ApiParam("Saksnummer må være et eksisterende saksnummer") @Valid SaksnummerDto s) {
        Saksnummer saksnummer = new Saksnummer(s.getVerdi());
        return behandlingDtoTjeneste.hentAlleBehandlinger(saksnummer);
    }

    @POST
    @Timed
    @ApiOperation(value = "Init hent behandling")
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Hent behandling initiert, Returnerer link til å polle på fremdrift", responseHeaders = {
            @ResponseHeader(name = "Location")}),
        @ApiResponse(code = 303, message = "Behandling tilgjenglig (prosesstasks avsluttet)", responseHeaders = {@ResponseHeader(name = "Location")})
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandling(@NotNull @Valid BehandlingIdDto idDto) throws URISyntaxException {
        // sender alltid til poll status slik at vi får sjekket på utestående prosess tasks også.
        return Redirect.tilBehandlingPollStatus(idDto.getBehandlingId(), Optional.empty());
    }

    @GET
    @Path("/status")
    @Timed
    @ApiOperation(value = "Url for å polle på behandling mens behandlingprosessen pågår i bakgrunnen(asynkront)", notes = ("Returnerer link til enten samme (hvis ikke ferdig) eller redirecter til /behandlinger dersom asynkrone operasjoner er ferdig."))
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returnerer Status", response = AsyncPollingStatus.class),
        @ApiResponse(code = 418, message = "ProsessTasks har feilet", response = AsyncPollingStatus.class, responseHeaders = {
            @ResponseHeader(name = "Location")}),
        @ApiResponse(code = 303, message = "Behandling tilgjenglig (prosesstasks avsluttet)", responseHeaders = {@ResponseHeader(name = "Location")})
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingMidlertidigStatus(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto idDto,
                                                    @Nullable @QueryParam("gruppe") @Valid ProsessTaskGruppeIdDto gruppeDto)
        throws URISyntaxException {
        Long behandlingId = idDto.getBehandlingId();
        String gruppe = gruppeDto == null ? null : gruppeDto.getGruppe();
        Behandling behandling = behandlingsprosessTjeneste.hentBehandling(behandlingId);
        Optional<AsyncPollingStatus> prosessTaskGruppePågår = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, gruppe);
        return Redirect.tilBehandlingEllerPollStatus(behandlingId, prosessTaskGruppePågår.orElse(null));
    }

    @GET
    @Timed
    @ApiOperation(value = "Hent behandling gitt id", notes = ("Returnerer behandlingen som er tilknyttet id. Dette er resultat etter at asynkrone operasjoner er utført."))
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returnerer Behandling", response = UtvidetBehandlingDto.class),
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingResultat(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto idDto) {

        var behandlingId = idDto.getBehandlingId();
        var behandling = behandlingsprosessTjeneste.hentBehandling(behandlingId);

        AsyncPollingStatus taskStatus = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, null).orElse(null);

        UtvidetBehandlingDto dto = behandlingDtoTjeneste.hentUtvidetBehandlingResultat(idDto.getBehandlingId(), taskStatus);

        Response.ResponseBuilder responseBuilder = Response.ok().entity(dto);
        return responseBuilder.build();
    }

    @POST
    @Path("/bytt-enhet")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Bytte behandlende enhet")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void byttBehandlendeEnhet(@ApiParam("Ny enhet som skal byttes") @Valid ByttBehandlendeEnhetDto dto) {
        Long behandlingId = dto.getBehandlingId();
        Long behandlingVersjon = dto.getBehandlingVersjon();
        behandlingTjeneste.kanEndreBehandling(behandlingId, behandlingVersjon);

        String enhetId = dto.getEnhetId();
        String enhetNavn = dto.getEnhetNavn();
        behandlendeEnhetTjeneste.byttBehandlendeEnhet(behandlingId, new OrganisasjonsEnhet(enhetId, enhetNavn), HistorikkAktør.SAKSBEHANDLER);
    }

}
