package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk;

import static no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk.HistorikkRestTjeneste.HISTORIKK_PATH;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.historikk.HistorikkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.historikk.HistorikkinnslagDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.SaksnummerDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path(HISTORIKK_PATH)
@ApplicationScoped
@Transactional
public class HistorikkRestTjeneste {

    public static final String HISTORIKK_PATH = "/historikk";

    private HistorikkTjeneste historikkTjeneste;

    public HistorikkRestTjeneste() {
        // Rest CDI
    }

    @Inject
    public HistorikkRestTjeneste(HistorikkTjeneste historikkTjeneste) {
        this.historikkTjeneste = historikkTjeneste;
    }

    @GET
    @Path("/v2")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Operation(tags = "historikk", description = "Henter alle historikkinnslag for gitt behandling.")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<HistorikkinnslagDto> hentAlleInnslagV2(@NotNull @QueryParam("saksnummer")
                                    @Parameter(description = "Saksnummer må være et eksisterende saksnummer")
                                    @Valid SaksnummerDto saksnummerDto) {
        return historikkTjeneste.hentForSak(new Saksnummer(saksnummerDto.getVerdi()));
    }

}
