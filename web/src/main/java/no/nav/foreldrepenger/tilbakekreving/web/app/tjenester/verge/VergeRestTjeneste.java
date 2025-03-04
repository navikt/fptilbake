package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge;

import java.net.URISyntaxException;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.dto.OpprettVerge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.Redirect;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.dto.NyVergeDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.dto.VergeDto;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@ApplicationScoped
@Path(VergeRestTjeneste.BASE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class VergeRestTjeneste {

    public static final String BASE_PATH = "/verge";


    private static final String VERGE_FJERN_PART_PATH = "/fjern-verge";
    public static final String VERGE_FJERN_PATH = BASE_PATH + VERGE_FJERN_PART_PATH;

    private static final String VERGE_OPPRETT_PART_PATH = "/opprett-verge";
    public static final String VERGE_OPPRETT_PATH = BASE_PATH + VERGE_OPPRETT_PART_PATH;

    private BehandlingTjeneste behandlingTjeneste;
    private VergeTjeneste vergeTjeneste;
    private PersoninfoAdapter tpsTjeneste;

    public VergeRestTjeneste() {
    }

    @Inject
    public VergeRestTjeneste(BehandlingTjeneste behandlingTjeneste,
                             VergeTjeneste vergeTjeneste,
                             PersoninfoAdapter tpsTjeneste) {
        this.behandlingTjeneste = behandlingTjeneste;
        this.vergeTjeneste = vergeTjeneste;
        this.tpsTjeneste = tpsTjeneste;
    }

    @GET
    @Operation(description = "Henter verge/fullmektig på behandlingen", tags = "verge", responses = {@ApiResponse(responseCode = "200", description = "Verge/fullmektig funnet"), @ApiResponse(responseCode = "204", description = "Ingen verge/fullmektig")})
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public VergeDto hentVerge(@QueryParam(UuidDto.NAME) @Parameter(description = "Behandling uuid") @Valid UuidDto queryParam) {
        var behandling = behandlingTjeneste.hentBehandling(queryParam.getBehandlingUuid());

        return vergeTjeneste.hentVergeInformasjon(behandling.getId()).map(v -> map(behandling.getFagsak().getFagsakYtelseType(), v)).orElse(null);
    }

    @POST
    @Path(VERGE_OPPRETT_PART_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Oppretter verge/fullmektig på behandlingen", tags = "verge", responses = {@ApiResponse(responseCode = "200", description = "Verge/fullmektig opprettes", headers = @Header(name = HttpHeaders.LOCATION))})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response opprettVerge(
            @QueryParam(UuidDto.NAME) @Parameter(description = "Behandling uuid") @Valid UuidDto queryParam,
            @Valid NyVergeDto body) {

        var behandling = behandlingTjeneste.hentBehandling(queryParam.getBehandlingUuid());

        vergeTjeneste.opprettVerge(behandling, map(body));

        return Response.ok().build();
    }

    @POST
    @Path(VERGE_FJERN_PART_PATH)
    @Operation(tags = "verge", description = "Fjerner verge/fullmektig på behandlingen", responses = {@ApiResponse(responseCode = "200", description = "Verge/fullmektig fjernet", headers = @Header(name = HttpHeaders.LOCATION))})
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response fjernVerge(@QueryParam(UuidDto.NAME) @Parameter(description = "Behandling uuid") @Valid UuidDto queryParam) {

        var behandling = behandlingTjeneste.hentBehandling(queryParam.getBehandlingUuid());

        vergeTjeneste.fjernVerge(behandling);

        return Response.ok().build();
    }


    @Deprecated
    @POST
    @Path("/opprett")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Oppretter aksjonspunkt for verge/fullmektig på behandlingen",
            tags = "verge",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Aksjonspunkt for verge/fullmektig opprettes",
                            headers = @Header(name = HttpHeaders.LOCATION)
                    )
            })
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response opprettVerge(@Context HttpServletRequest request,
                                 @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                 @Parameter(description = "Behandling som skal få verge/fullmektig") @Valid BehandlingReferanse dto) throws URISyntaxException {
        Behandling behandling = hentBehandling(dto);
        if (behandling.erSaksbehandlingAvsluttet() || behandling.isBehandlingPåVent()) {
            throw new TekniskException("FPT-763493", String.format("Behandlingen er allerede avsluttet eller sett på vent, kan ikke opprette verge for behandling %s", behandling.getId()));
        }
        if (!behandling.getÅpneAksjonspunkter(List.of(AksjonspunktDefinisjon.AVKLAR_VERGE)).isEmpty()) {
            throw new TekniskException("FPT-185321", String.format("Behandling %s har allerede aksjonspunkt 5030 for verge/fullmektig", behandling.getId()));
        }
        vergeTjeneste.opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(behandling);
        return Redirect.tilBehandlingPollStatus(request, behandling.getUuid());
    }

    @Deprecated
    @POST
    @Path("/fjern")
    @Operation(description = "Fjerner aksjonspunkt og evt. registrert informasjon om verge/fullmektig fra behandlingen",
            tags = "verge",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Fjerning av verge/fullmektig er gjennomført",
                            headers = @Header(name = HttpHeaders.LOCATION)
                    )
            })
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response fjernVerge(@Context HttpServletRequest request,
                               @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                               @Parameter(description = "Behandling som skal få fjernet verge/fullmektig") @Valid BehandlingReferanse dto) throws URISyntaxException {
        Behandling behandling = hentBehandling(dto);
        if (behandling.erSaksbehandlingAvsluttet() || behandling.isBehandlingPåVent()) {
            throw new TekniskException("FPT-763494", String.format("Behandlingen er allerede avsluttet eller sett på vent, kan ikke fjerne verge for behandling %s", behandling.getId()));
        }
        vergeTjeneste.fjernVergeGrunnlagOgAksjonspunkt(behandling);
        return Redirect.tilBehandlingPollStatus(request, behandling.getUuid());
    }

    private VergeDto map(FagsakYtelseType ytelseType, VergeEntitet vergeEntitet) {
        VergeDto vergeDto = new VergeDto();
        if (vergeEntitet.getVergeType().equals(VergeType.ADVOKAT)) {
            vergeDto.setOrganisasjonsnummer(vergeEntitet.getOrganisasjonsnummer());
        } else {
            tpsTjeneste.hentBrukerForAktør(ytelseType, vergeEntitet.getVergeAktørId())
                    .ifPresent(value -> vergeDto.setFnr(value.getPersonIdent().getIdent()));
        }
        vergeDto.setGyldigFom(vergeEntitet.getGyldigFom());
        vergeDto.setGyldigTom(vergeEntitet.getGyldigTom());
        vergeDto.setNavn(vergeEntitet.getNavn());
        vergeDto.setVergeType(vergeEntitet.getVergeType());
        vergeDto.setBegrunnelse(vergeEntitet.getBegrunnelse());
        return vergeDto;
    }

    private Behandling hentBehandling(BehandlingReferanse behandlingReferanse) {
        Behandling behandling;
        if (behandlingReferanse.erInternBehandlingId()) {
            behandling = behandlingTjeneste.hentBehandling(behandlingReferanse.getBehandlingId());
        } else {
            behandling = behandlingTjeneste.hentBehandling(behandlingReferanse.getBehandlingUuid());
        }
        return behandling;
    }

    private OpprettVerge map(NyVergeDto dto) {
        return new OpprettVerge(dto.getNavn(), dto.getFnr(), dto.getGyldigFom(), dto.getGyldigTom(), dto.getVergeType(),
                dto.getOrganisasjonsnummer(), null);
    }
}
