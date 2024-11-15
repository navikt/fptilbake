package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkV2Tjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk.HistorikkV2RestTjeneste.HISTORIKK_PATH;

@Path(HISTORIKK_PATH)
@ApplicationScoped
@Transactional
public class HistorikkV2RestTjeneste {

    public static final String HISTORIKK_PATH = "/historikk";

    private HistorikkV2Tjeneste historikkTjeneste;

    public HistorikkV2RestTjeneste() {
        // Rest CDI
    }

    @Inject
    public HistorikkV2RestTjeneste(HistorikkV2Tjeneste historikkTjeneste) {
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

        var path = HistorikkRequestPath.getRequestPath(request);

        var historikkInnslagDtoList = historikkTjeneste.hentForSak(new Saksnummer(saksnummerDto.getVerdi()), path);
        return Response.ok().entity(historikkInnslagDtoList).build();
    }

}
