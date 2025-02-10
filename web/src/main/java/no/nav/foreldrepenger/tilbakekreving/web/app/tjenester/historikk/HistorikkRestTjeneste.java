package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk;

import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk.HistorikkRestTjeneste.HISTORIKK_PATH;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkV2Tjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path(HISTORIKK_PATH)
@ApplicationScoped
@Transactional
public class HistorikkRestTjeneste {

    public static final String HISTORIKK_PATH = "/historikk";

    private HistorikkTjenesteAdapter historikkTjeneste;
    private HistorikkV2Tjeneste historikkV2Tjeneste;

    public HistorikkRestTjeneste() {
        // Rest CDI
    }

    @Inject
    public HistorikkRestTjeneste(HistorikkTjenesteAdapter historikkTjeneste, HistorikkV2Tjeneste historikkV2Tjeneste) {
        this.historikkTjeneste = historikkTjeneste;
        this.historikkV2Tjeneste = historikkV2Tjeneste;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Operation(tags = "historikk", description = "Henter alle historikkinnslag for gitt behandling.")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentAlleInnslag(@Context HttpServletRequest request,
                                    @NotNull @QueryParam("saksnummer")
                                    @Parameter(description = "Saksnummer må være et eksisterende saksnummer")
                                    @Valid SaksnummerDto saksnummerDto) {

        var path = HistorikkRequestPath.getRequestPath(request);

        var historikkInnslagDtoList = historikkTjeneste.hentAlleHistorikkInnslagForSak(new Saksnummer(saksnummerDto.getVerdi()), path);
        return Response.ok().entity(historikkInnslagDtoList).build();
    }


    @GET
    @Path("/v2")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Operation(tags = "historikk", description = "Henter alle historikkinnslag for gitt behandling.")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentAlleInnslagV2(@Context HttpServletRequest request,
                                    @NotNull @QueryParam("saksnummer")
                                    @Parameter(description = "Saksnummer må være et eksisterende saksnummer")
                                    @Valid SaksnummerDto saksnummerDto) {

        var path = HistorikkRequestPath.getRequestPath(request);

        var historikkInnslagDtoList = historikkV2Tjeneste.hentForSak(new Saksnummer(saksnummerDto.getVerdi()), path);
        return Response.ok().entity(historikkInnslagDtoList).build();
    }

}
