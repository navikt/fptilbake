package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.konfig;

import java.net.URI;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/konfig")
@RequestScoped
@Transactional
public class KonfigRestTjeneste {

    private URI rettskildeUrl;
    private URI systemrutineUrl;

    public KonfigRestTjeneste() {
        //NOSONAR
    }

    @Inject
    public KonfigRestTjeneste(@KonfigVerdi(value = "rettskilde.url") URI rettskildeUrl, @KonfigVerdi(value = "systemrutine.url") URI systemrutineUrl) {
        this.rettskildeUrl = rettskildeUrl;
        this.systemrutineUrl = systemrutineUrl;
    }

    @GET
    @Path("/rettskilde")
    @Produces("application/json")
    @Operation(tags = "konfigurasjon", description = "Henter lenke til rettskilde.")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Konfig hentRettskildeUrl() {
        return new Konfig(rettskildeUrl.toString());
    }

    @GET
    @Path("/systemrutine")
    @Produces("application/json")
    @Operation(tags = "konfigurasjon", description = "Henter lenge til systemrutine")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Konfig hentSystemrutine() {
        return new Konfig(systemrutineUrl.toString());
    }

    public static class Konfig {

        private String verdi;

        public Konfig(String verdi) {
            this.verdi = verdi;
        }

        public String getVerdi() {
            return verdi;
        }
    }
}
