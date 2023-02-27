package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/historikk")
@ApplicationScoped
@Transactional
public class HistorikkRestTjeneste {

    public static final String HISTORIKK_PATH = "/historikk";

    private HistorikkTjenesteAdapter historikkTjeneste;

    public HistorikkRestTjeneste() {
        // Rest CDI
    }

    @Inject
    public HistorikkRestTjeneste(HistorikkTjenesteAdapter historikkTjeneste) {
        this.historikkTjeneste = historikkTjeneste;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Operation(tags = "historikk", description = "Henter alle historikkinnslag for gitt behandling.")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentAlleInnslag(@Context HttpServletRequest request,
                                    @NotNull @QueryParam("saksnummer")
                                    @Parameter(description = "Saksnummer må være et eksisterende saksnummer")
                                    @Valid SaksnummerDto saksnummerDto) {

        var path = historikkTjeneste.getRequestPath(request);

        var historikkInnslagDtoList = historikkTjeneste.hentAlleHistorikkInnslagForSak(new Saksnummer(saksnummerDto.getVerdi()), path);
        return Response.ok().entity(historikkInnslagDtoList).build();
    }

}
