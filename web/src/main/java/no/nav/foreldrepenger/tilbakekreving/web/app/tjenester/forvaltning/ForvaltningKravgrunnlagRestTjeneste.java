package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.HentKorrigertKravgrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/forvaltning/kravgrunnlag")
@ApplicationScoped
@Transactional
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ForvaltningKravgrunnlagRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ForvaltningKravgrunnlagRestTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private ForvaltningTjeneste forvaltningTjeneste;
    private KravgrunnlagRepository kravgrunnlagRepository;

    public ForvaltningKravgrunnlagRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningKravgrunnlagRestTjeneste(BehandlingRepository behandlingRepository,
                                               ForvaltningTjeneste forvaltningTjeneste,
                                               KravgrunnlagRepository kravgrunnlagRepository) {
        this.behandlingRepository = behandlingRepository;
        this.forvaltningTjeneste = forvaltningTjeneste;
        this.kravgrunnlagRepository = kravgrunnlagRepository;
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
    public Response hentKorrigertKravgrunnlag(@Valid @NotNull @TilpassetAbacAttributt(supplierClass = AbacIngen.class) HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto) {
        Behandling behandling = behandlingRepository.hentBehandling(hentKorrigertKravgrunnlagDto.getBehandlingId());
        if (behandling.erAvsluttet()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Kan ikke hente korrigert kravgrunnlag, behandlingen er avsluttet").build();
        }

        String kravgrunnlagId;
        if (hentKorrigertKravgrunnlagDto.getKravgrunnlagId() != null) {
            kravgrunnlagId = hentKorrigertKravgrunnlagDto.getKravgrunnlagId();
        } else {
            Optional<Kravgrunnlag431> eksisterendeKravgrunnlag = kravgrunnlagRepository.finnKravgrunnlagOpt(behandling.getId());
            if (eksisterendeKravgrunnlag.isPresent()) {
                kravgrunnlagId = eksisterendeKravgrunnlag.get().getEksternKravgrunnlagId();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Behandlingen har ikke eksisterende kravgrunnlag. kravgrunnlagId kreves da for å hente kravgrunnlag.")
                    .build();
            }
        }

        forvaltningTjeneste.hentKorrigertKravgrunnlag(behandling, kravgrunnlagId);
        return Response.ok().build();
    }

    @PUT
    @Path("/annuler")
    @Operation(
        tags = "FORVALTNING-kravgrunnlag",
        description = "Tjeneste for å annullere en mottatt kravgrunnlag hos økonomi, f.eks pga brukerens død",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Finnes ikke kravgrunnlag."),
            @ApiResponse(responseCode = "500", description = "Ukjent feil!")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response annullerKravgrunnlag(@Valid @NotNull @TilpassetAbacAttributt(supplierClass = AbacIngen.class) HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto) {
        Behandling behandling = behandlingRepository.hentBehandling(hentKorrigertKravgrunnlagDto.getBehandlingId());
        try {
            var behandlingId = behandling.getId();
            LOG.info("Starter Anullerekravgrunnlag for behandlingId={}", behandlingId);
            forvaltningTjeneste.annullerKravgrunnlag(behandlingId);
            LOG.info("AnnulereKravgrunnlag sendt til oppdragssystemet.");
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
    public Response hentForvaltninginfo(@NotNull @QueryParam("saksnummer") SaksnummerDto saksnummer) {
        try {
            return Response.ok(forvaltningTjeneste.hentForvaltningsinfo(new Saksnummer(saksnummer.getVerdi()))).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    /**
     * bruk bare på tjenester hvor det ikke er relevant å sjekke subjektet. Dette gjelder typisk kun
     * rent tekniske tjenester som ikke returnerer persondata. Kan brukes på for eksempel:
     * Rekjøre prosesstask, trigge innhenting av oppdatert kravgrunnlag
     */
    public static class AbacIngen implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }

}
