package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import com.codahale.metrics.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.KlageTilbakekrevingDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

@Path(BehandlingBackendRestTjeneste.PATH_FRAGMENT)
@RequestScoped
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class BehandlingBackendRestTjeneste {

    public static final String PATH_FRAGMENT = "/behandlinger-backend";

    private BehandlingTjeneste behandlingTjeneste;
    private BehandlingVedtakRepository behandlingVedtakRepository;

    public BehandlingBackendRestTjeneste() {
        // for CDI
    }

    @Inject
    public BehandlingBackendRestTjeneste(BehandlingTjeneste behandlingTjeneste, BehandlingVedtakRepository behandlingVedtakRepository){
        this.behandlingTjeneste = behandlingTjeneste;
        this.behandlingVedtakRepository = behandlingVedtakRepository;
    }

    @GET
    @Path("/tilbakekreving/aapen")
    @Timed
    @Operation(
        tags = "behandlinger-backend",
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

    @GET
    @Path("/tilbakekreving/vedtak-info")
    @Timed
    @Operation(
        tags = "behandlinger-backend",
        description = "Hent tilbakekrevingsvedtakInfo",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returnerer vedtak info for tilbakekreving", content = @Content(schema = @Schema(implementation = Boolean.class)))
        })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response hentTilbakekrevingsVedtakInfo(@NotNull @QueryParam(UuidDto.NAME) @Parameter(description = UuidDto.DESC) @Valid UuidDto uuidDto) {
            UUID behandlingUUId = uuidDto.getBehandlingUuid();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingUUId);
        Long behandlingId = behandling.getId();
        Optional<BehandlingVedtak> behandlingVedtak= behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
        if(!behandling.erAvsluttet() || behandlingVedtak.isEmpty()){
            throw BehandlingFeil.FACTORY.fantIkkeBehandlingsVedtakInfo(behandlingId).toException();
        }
        KlageTilbakekrevingDto klageTilbakekrevingDto = new KlageTilbakekrevingDto(behandlingId, behandlingVedtak.get().getVedtaksdato(),behandling.getType().getKode());
        return Response.ok().entity(klageTilbakekrevingDto).build();
    }
}
