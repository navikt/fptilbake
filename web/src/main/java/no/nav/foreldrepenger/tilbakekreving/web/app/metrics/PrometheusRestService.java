package no.nav.foreldrepenger.tilbakekreving.web.app.metrics;


import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static no.nav.vedtak.log.metrics.MetricsUtil.REGISTRY;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Operation;

@Path("/metrics")
@Produces(TEXT_PLAIN)
@ApplicationScoped
public class PrometheusRestService {

    @GET
    @Operation(tags = "metrics", hidden = true)
    @Path("/prometheus")
    public String prometheus() {
        return REGISTRY.scrape();
    }
}
