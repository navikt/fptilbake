package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.init;

import static no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLinks.get;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLink;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk.HistorikkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path("/init-fetch")
@ApplicationScoped
@Transactional

@Produces(MediaType.APPLICATION_JSON)
public class InitielleLinksRestTjeneste {

    private String kontekstPath;

    @Inject
    public InitielleLinksRestTjeneste() {
        var applikasjon = ApplicationName.hvilkenTilbake();
        kontekstPath = switch (applikasjon) {
            case FPTILBAKE -> "/fptilbake";
            case K9TILBAKE -> "/k9/tilbake";
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        };
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Returnerer lenker til init av frontend", tags = "init-fetch")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.APPLIKASJON, sporingslogg = false)
    public InitLinksDto hentInitielleRessurser() {
        List<ResourceLink> lenkene = new ArrayList<>();
        lenkene.add(get(KodeverkRestTjeneste.KODERVERK_PATH, "tilbake-kodeverk"));
        List<ResourceLink> saklenker = new ArrayList<>();
        saklenker.add(get(BehandlingRestTjeneste.SAK_FULL_PATH, "tilbake-fagsak-full"));
        saklenker.add(get(HistorikkRestTjeneste.HISTORIKK_PATH + "/v2", "tilbake-historikkinnslag"));
        saklenker.add(get(BehandlingRestTjeneste.SAK_RETTIGHETER_PATH, "tilbake-sak-rettigheter"));
        saklenker.add(get(BehandlingRestTjeneste.BEHANDLING_ALLE_PATH, "tilbake-alle-behandlinger"));
        saklenker.add(get(BehandlingRestTjeneste.BEHANDLING_KAN_OPPRETTES_PATH, "tilbake-kan-opprette-behandling"));
        saklenker.add(get(BehandlingRestTjeneste.REVURDERING_KAN_OPPRETTES_PATH, "tilbake-kan-opprette-revurdering"));
        return new InitLinksDto(lenkene, List.of(), saklenker);
    }
}
