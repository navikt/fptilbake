package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import no.nav.foreldrepenger.tilbakekreving.web.app.metrics.PrometheusRestService;
import no.nav.foreldrepenger.tilbakekreving.web.app.healthchecks.HealthCheckRestService;

@ApplicationPath(InternalApiConfig.API_URL)
public class InternalApiConfig extends Application {

    public static final String API_URL = "/internal";

    InternalApiConfig() {
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(HealthCheckRestService.class, PrometheusRestService.class);
    }
}
