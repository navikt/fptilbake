package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.konfig;

import java.net.URI;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
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
