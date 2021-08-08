package no.nav.foreldrepenger.tilbakekreving.web.local.development;

import java.net.URI;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.v3.oas.annotations.Operation;

/**
 * Innlogging ved kjøring lokalt.
 * <p>
 * Se utviklerhåndbok for hvordan dette fungerer.
 */
@Path("/login")
@RequestScoped
public class JettyLoginResource {

    @GET
    @Timed
    // Re-enable dersom non-empty. jersey gir warning @Path("")
    @Operation(tags = "login", description = "brukes for å strømlinjeforme innlogging ved lokal testing", hidden = true)
    public Response login() {
        //  når vi har kommet hit, er brukeren innlogget og har fått ID-token. Kan da gjøre redirect til hovedsiden for VL
        return Response.temporaryRedirect(URI.create("http://localhost:9000/")).build();
    }
}
