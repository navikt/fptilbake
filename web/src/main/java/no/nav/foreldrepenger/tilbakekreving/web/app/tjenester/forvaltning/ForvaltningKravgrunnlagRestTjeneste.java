package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.HentKorrigertKravgrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/forvaltning/kravgrunnlag")
@ApplicationScoped
@Transactional
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ForvaltningKravgrunnlagRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(ForvaltningKravgrunnlagRestTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private ForvaltningTjeneste forvaltningTjeneste;

    public ForvaltningKravgrunnlagRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningKravgrunnlagRestTjeneste(BehandlingRepository behandlingRepository,
                                               ForvaltningTjeneste forvaltningTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.forvaltningTjeneste = forvaltningTjeneste;
    }

    @POST
    @Path("/hent-korrigert")
    @Operation(
        tags = "FORVALTNING-kravgrunnlag",
        description = "Tjeneste for å hente korrigert kravgrunnlag for en behandling",
        responses = {
            @ApiResponse(responseCode = "200", description = "Hent korrigerte grunnlag og tilkoblet det med en behandling"),
            @ApiResponse(responseCode = "400", description = "Behandling er avsluttet eller ikke gyldig")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response hentKorrigertKravgrunnlag(@Valid @NotNull HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto) {
        Behandling behandling = behandlingRepository.hentBehandling(hentKorrigertKravgrunnlagDto.getBehandlingId());
        if (behandling.erAvsluttet()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Kan ikke hente korrigert kravgrunnlag, behandlingen er avsluttet").build();
        }
        logger.info("Oppretter task for å hente korrigert kravgrunnlag for behandlingId={}", behandling.getId());
        forvaltningTjeneste.hentKorrigertKravgrunnlag(behandling, hentKorrigertKravgrunnlagDto.getKravgrunnlagId());
        return Response.ok().build();
    }

    @PUT
    @Path("/annuler")
    @Operation(
        tags = "FORVALTNING-kravgrunnlag",
        description = "Tjeneste for å annulere en mottatt kravgrunnlag hos økonomi, f.eks pga brukerens død",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Finnes ikke kravgrunnlag."),
            @ApiResponse(responseCode = "500", description = "Ukjent feil!")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response annulerKravgrunnlag(@Valid @NotNull HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto) {
        Behandling behandling = behandlingRepository.hentBehandling(hentKorrigertKravgrunnlagDto.getBehandlingId());
        try {
            forvaltningTjeneste.annulerKravgrunnlag(behandling.getId());
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/forvaltninginfo")
    @Operation(
        tags = "FORVALTNING-kravgrunnlag",
        description = "Hent informasjon som kreves for forvaltning",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Ukjent feil!")
        })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.DRIFT)
    public Response hentForvaltninginfo(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataSaksnummerReferanse.class)
                                        @NotNull @QueryParam("saksnummer") @Valid SaksnummerDto saksnummer) {
        try {
            return Response.ok(forvaltningTjeneste.hentForvaltningsinfo(saksnummer.getVerdi())).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
