package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;

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
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktDtoMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.Redirect;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path(AksjonspunktRestTjeneste.BASE_PATH)
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class AksjonspunktRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(AksjonspunktRestTjeneste.class);

    static final String BASE_PATH = "/behandling";
    private static final String AKSJONSPUNKT_BESLUTT_PART_PATH = "/aksjonspunkt/beslutt";
    private static final String AKSJONSPUNKT_PART_PATH = "/aksjonspunkt";
    public static final String AKSJONSPUNKT_BESLUTT_PATH = BASE_PATH + AKSJONSPUNKT_BESLUTT_PART_PATH;
    public static final String AKSJONSPUNKT_PATH = BASE_PATH + AKSJONSPUNKT_PART_PATH;

    private BehandlingRepository behandlingRepository;
    private TotrinnRepository totrinnRepository;
    private BehandlingTjeneste behandlingTjeneste;
    private AksjonspunktApplikasjonTjeneste aksjonspunktApplikasjonTjeneste;

    public AksjonspunktRestTjeneste() {
        // CDI
    }

    @Inject
    public AksjonspunktRestTjeneste(BehandlingRepository behandlingRepository,
                                    TotrinnRepository totrinnRepository,
                                    BehandlingTjeneste behandlingTjeneste,
                                    AksjonspunktApplikasjonTjeneste aksjonspunktApplikasjonTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.totrinnRepository = totrinnRepository;
        this.behandlingTjeneste = behandlingTjeneste;
        this.aksjonspunktApplikasjonTjeneste = aksjonspunktApplikasjonTjeneste;
    }

    @GET
    @Path(AKSJONSPUNKT_PART_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "aksjonspunkt",
            description = "Hent aksjonspunter for en behandling",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Aksjonspunkter", content = @Content(array = @ArraySchema(uniqueItems = true, arraySchema = @Schema(implementation = Set.class), schema = @Schema(implementation = AksjonspunktDto.class)), mediaType = MediaType.APPLICATION_JSON))
            })
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response getAksjonspunkter(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                      @NotNull @QueryParam("uuid") @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        Collection<Totrinnsvurdering> totrinnsvurderinger = totrinnRepository.hentTotrinnsvurderinger(behandling);
        Set<AksjonspunktDto> dto = AksjonspunktDtoMapper.lagAksjonspunktDto(behandling, totrinnsvurderinger);
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoStore(true);
        cc.setMaxAge(0);
        return Response.ok(dto).cacheControl(cc).build();
    }

    /**
     * Håndterer prosessering av aksjonspunkt og videre behandling.
     * <p>
     * MERK: Det skal ikke ligge spesifikke sjekker som avhenger av status på behanlding, steg eller knytning til
     * spesifikke aksjonspunkter idenne tjenesten.
     *
     * @throws URISyntaxException
     */
    @POST
    @Path(AKSJONSPUNKT_PART_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            tags = "aksjonspunkt",
            description = "Lagre endringer gitt av aksjonspunktene og rekjør behandling fra gjeldende steg")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response bekreft(@Context HttpServletRequest request,
                            @Parameter(description = "Liste over aksjonspunkt som skal bekreftes, inklusiv data som trengs for å løse de.") @Valid BekreftedeAksjonspunkterDto apDto) throws URISyntaxException {
        if (fatterVedtak(apDto.getBekreftedeAksjonspunktDtoer())) {
            throw new IllegalArgumentException("Fatter vedtak aksjonspunkt løses i eget endepunkt");
        }
        return bekreftAksjonspunkt(request, apDto);
    }

    private static boolean fatterVedtak(Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer) {
        return bekreftedeAksjonspunktDtoer.stream().anyMatch(dto -> dto.getAksjonspunktDefinisjon() == AksjonspunktDefinisjon.FATTE_VEDTAK);
    }

    private Response bekreftAksjonspunkt(HttpServletRequest request, BekreftedeAksjonspunkterDto apDto) throws URISyntaxException {
        BehandlingReferanse behandlingReferanse = apDto.getBehandlingReferanse();
        Behandling behandling = hentBehandling(behandlingReferanse);
        Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer = apDto.getBekreftedeAksjonspunktDtoer();
        behandlingTjeneste.kanEndreBehandling(behandling.getId(), apDto.getBehandlingVersjon());
        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(bekreftedeAksjonspunktDtoer, behandling.getId());
        return Redirect.tilBehandlingPollStatus(request, behandling.getUuid());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(AKSJONSPUNKT_BESLUTT_PART_PATH)
    @Operation(
        tags = "aksjonspunkt",
        description = "Lagre endringer gitt av aksjonspunktene og rekjør behandling fra gjeldende steg")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public Response beslutt(@Context HttpServletRequest request,
                            @Parameter(description = "Liste over aksjonspunkt som skal bekreftes, inklusiv data som trengs for å løse de.") @Valid BekreftedeAksjonspunkterDto apDto) throws URISyntaxException {
        var bekreftedeAksjonspunktDtoer = apDto.getBekreftedeAksjonspunktDtoer();
        if (bekreftedeAksjonspunktDtoer.size() > 1) {
            throw new IllegalArgumentException("Forventer kun ett aksjonspunkt");
        }
        if (!fatterVedtak(bekreftedeAksjonspunktDtoer)) {
            throw new IllegalArgumentException("Forventer aksjonspunkt FATTE_VEDTAK");
        }
        return bekreftAksjonspunkt(request, apDto);
    }

    private Behandling hentBehandling(BehandlingReferanse behandlingReferanse) {
        Behandling behandling;
        if (behandlingReferanse.erInternBehandlingId()) {
            behandling = behandlingRepository.hentBehandling(behandlingReferanse.getBehandlingId());
        } else {
            behandling = behandlingRepository.hentBehandling(behandlingReferanse.getBehandlingUuid());
        }
        return behandling;
    }
}
