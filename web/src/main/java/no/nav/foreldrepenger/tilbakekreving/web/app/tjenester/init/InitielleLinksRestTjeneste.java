package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.init;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.web.app.rest.ResourceLink;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.historikk.HistorikkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/init-fetch")
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class InitielleLinksRestTjeneste {

    private static final String API_URI = "/api";

    private String kontekstPath;

    public InitielleLinksRestTjeneste() {
    }

    @Inject
    public InitielleLinksRestTjeneste(@KonfigVerdi(value = "app.name") String applikasjon) {
        switch (applikasjon) {
            case "fptilbake":
                kontekstPath = "/fptilbake";
                break;
            case "k9-tilbake":
                kontekstPath = "/k9/tilbake";
                break;
            default:
                throw new IllegalStateException("app.name er satt til " + applikasjon + " som ikke er en støttet verdi");
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Returnerer lenker til init av frontend", tags = "init-fetch")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public InitLinksDto hentInitielleRessurser() {
        List<ResourceLink> lenkene = new ArrayList<>();
        lenkene.add(get(KodeverkRestTjeneste.KODERVERK_PATH, "tilbake-kodeverk"));
        List<ResourceLink> saklenker = new ArrayList<>();
        saklenker.add(get(HistorikkRestTjeneste.HISTORIKK_PATH, "tilbake-historikk"));
        saklenker.add(get(BehandlingRestTjeneste.SAK_RETTIGHETER_PATH, "tilbake-sak-rettigheter"));
        saklenker.add(get(BehandlingRestTjeneste.BEHANDLING_ALLE_PATH, "tilbake-alle-behandlinger"));
        saklenker.add(get(BehandlingRestTjeneste.BEHANDLING_KAN_OPPRETTES_PATH, "tilbake-kan-opprette-behandling"));
        saklenker.add(get(BehandlingRestTjeneste.REVURDERING_KAN_OPPRETTES_PATH, "tilbake-kan-opprette-revurdering"));
        saklenker.add(get(BehandlingRestTjeneste.HANDLING_RETTIGHETER_PATH, "tilbake-handling-rettigheter-v2")); // TODO: Remove in contract-phase
        return new InitLinksDto(lenkene, List.of(), saklenker);
    }

    private ResourceLink get(String url, String relasjon) {
        return ResourceLink.get(kontekstPath + API_URI + url, relasjon, null);
    }

}
