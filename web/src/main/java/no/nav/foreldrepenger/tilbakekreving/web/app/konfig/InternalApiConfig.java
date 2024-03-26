package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import no.nav.foreldrepenger.tilbakekreving.web.app.healthchecks.HealthCheckRestService;
import no.nav.foreldrepenger.tilbakekreving.web.app.metrics.PrometheusRestService;

@ApplicationPath(InternalApiConfig.API_URI)
public class InternalApiConfig extends Application {

    public static final String API_URI = "/internal";

    InternalApiConfig() {
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(HealthCheckRestService.class, PrometheusRestService.class);
    }
}
