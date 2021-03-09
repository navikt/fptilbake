package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.Selftests;

@Path("/health")
@ApplicationScoped
public class HealthCheckRestService {

    private static final String RESPONSE_CACHE_KEY = "Cache-Control";
    private static final String RESPONSE_CACHE_VAL = "must-revalidate,no-cache,no-store";
    private static final String RESPONSE_OK = "OK";

    private Selftests selftests;

    private Boolean isContextStartupReady = false;

    public HealthCheckRestService() {
        // CDI
    }

    @Inject
    public HealthCheckRestService(Selftests selftests) {
        this.selftests = selftests;
    }

    @GET
    @Path("isAlive")
    @Operation(tags = "nais", description = "sjekker om applikasjonen er i live", hidden = true)
    public Response isAlive() {
        if (isContextStartupReady) {
            return Response
                .ok(RESPONSE_OK, MediaType.TEXT_PLAIN_TYPE)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
        }
        return Response
            .serverError()
            .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
            .build();
    }

    @GET
    @Path("isReady")
    @Operation(tags = "nais", description = "sjekker om applikasjonen er klar", hidden = true)
    public Response isReady() {
        if (isContextStartupReady && selftests.isReady()) {
            return Response.ok(RESPONSE_OK)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
        }
    }

    /**
     * Settes av AppstartupServletContextListener ved contextInitialized
     *
     * @param isContextStartupReady
     */
    public void setIsContextStartupReady(Boolean isContextStartupReady) {
        this.isContextStartupReady = isContextStartupReady;
    }
}
