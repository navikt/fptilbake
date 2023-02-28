package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge;

import java.net.URISyntaxException;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.Redirect;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@ApplicationScoped
@Path(VergeRestTjeneste.BASE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class VergeRestTjeneste {

    static final String BASE_PATH = "/verge";
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
    @BeskyttetRessurs(actionType = ActionType.UPDATE, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response opprettVerge(@Context HttpServletRequest request,
                                 @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                 @Parameter(description = "Behandling som skal få verge/fullmektig") @Valid BehandlingReferanse dto) throws URISyntaxException {
        Behandling behandling = hentBehandling(dto);
        if (behandling.erSaksbehandlingAvsluttet() || behandling.isBehandlingPåVent()) {
            throw new TekniskException("FPT-763493", String.format("Behandlingen er allerede avsluttet eller sett på vent, kan ikke opprette verge for behandling %s", behandling.getId()));
        }
        if (!behandling.getÅpneAksjonspunkter(Lists.newArrayList(AksjonspunktDefinisjon.AVKLAR_VERGE)).isEmpty()) {
            throw new TekniskException("FPT-185321", String.format("Behandling %s har allerede aksjonspunkt 5030 for verge/fullmektig", behandling.getId()));
        }
        vergeTjeneste.opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(behandling);
        return Redirect.tilBehandlingPollStatus(request, behandling.getUuid());
    }

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
    @BeskyttetRessurs(actionType = ActionType.UPDATE, property = AbacProperty.FAGSAK)
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

    @GET
    // Re-enable dersom non-empty. jersey gir warning @Path("/")
    @Operation(description = "Returnerer informasjon om verge knyttet til søker for denne behandlingen",
            tags = "verge",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returnerer Verge, null hvis ikke eksisterer (GUI støtter ikke NOT_FOUND p.t.)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = VergeDto.class)
                            )
                    )
            })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public VergeDto getVerge(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                             @QueryParam(value = "uuid") @NotNull @Valid BehandlingReferanse dto) {
        Long behandlingId = hentBehandlingId(dto);
        Optional<VergeEntitet> vergeEntitet = vergeTjeneste.hentVergeInformasjon(behandlingId);
        return vergeEntitet.isPresent() ? map(vergeEntitet.get()) : null;
    }

    @GET
    @Path("/behandlingsmeny")
    @Operation(description = "Instruerer hvilket menyvalg som skal være mulig fra behandlingsmenyen",
            tags = "verge",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Returnerer OPPRETT/FJERN",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = VergeBehandlingsmenyDto.class)
                            )
                    )
            })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingsmenyvalg(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                            @NotNull @QueryParam("uuid") @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        Optional<VergeEntitet> vergeEntitet = vergeTjeneste.hentVergeInformasjon(behandling.getId());
        boolean kanBehandlingEndres = !behandling.erSaksbehandlingAvsluttet() && !behandling.isBehandlingPåVent();
        boolean finnesVerge = vergeEntitet.isPresent();
        VergeBehandlingsmenyDto dto = new VergeBehandlingsmenyDto(behandling.getId(), VergeBehandlingsmenyEnum.SKJUL);
        if (kanBehandlingEndres) {
            dto = finnesVerge ? new VergeBehandlingsmenyDto(behandling.getId(), VergeBehandlingsmenyEnum.FJERN) :
                    new VergeBehandlingsmenyDto(behandling.getId(), VergeBehandlingsmenyEnum.OPPRETT);
        }
        return Response.ok(dto).build();
    }

    private VergeDto map(VergeEntitet vergeEntitet) {
        VergeDto vergeDto = new VergeDto();
        if (vergeEntitet.getVergeType().equals(VergeType.ADVOKAT)) {
            vergeDto.setOrganisasjonsnummer(vergeEntitet.getOrganisasjonsnummer());
        } else {
            Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(vergeEntitet.getVergeAktørId());
            if (personinfo.isPresent()) {
                vergeDto.setFnr(personinfo.get().getPersonIdent().getIdent());
            }
        }
        vergeDto.setGyldigFom(vergeEntitet.getGyldigFom());
        vergeDto.setGyldigTom(vergeEntitet.getGyldigTom());
        vergeDto.setNavn(vergeEntitet.getNavn());
        vergeDto.setVergeType(vergeEntitet.getVergeType());
        vergeDto.setBegrunnelse(vergeEntitet.getBegrunnelse());
        return vergeDto;
    }

    private Long hentBehandlingId(BehandlingReferanse dto) {
        return dto.erInternBehandlingId() ? dto.getBehandlingId() : hentBehandling(dto).getId();
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
}
