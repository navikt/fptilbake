package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.konfig;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.net.URI;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.codahale.metrics.annotation.Timed;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

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
    @Timed
    @Path("/rettskilde")
    @Produces("application/json")
    @Operation(tags = "konfigurasjon", description = "Henter lenke til rettskilde.")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Konfig hentRettskildeUrl() {
        return new Konfig(rettskildeUrl.toString());
    }

    @GET
    @Timed
    @Path("/systemrutine")
    @Produces("application/json")
    @Operation(tags = "konfigurasjon", description = "Henter lenge til systemrutine")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK, sporingslogg = false)
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
