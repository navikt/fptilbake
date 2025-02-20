package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.openapi;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;

import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.ApiConfig;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * OpenApiResource frå openapi-spec-utils er laga mtp eksplisitt initialisering med resolved OpenAPI instans.
 * <p>
 * Denne subklasse er laga for at den skal kunne autoinitialiserast av rammeverket uten at det blir fleire initialiseringer av ApiConfig.
 */
@ApplicationScoped
public class OpenApiTjeneste extends no.nav.openapi.spec.utils.openapi.OpenApiResource {
    @Context
    private Application application;

    public OpenApiTjeneste() {
        super(null);
    }

    // Det ser ut til å vere ein bug i jersey som gjere at Application blir satt til ein ResourceConfig instans.
    // Må derfor unwrappe frå denne før sjekk + cast til ApiConfig viss det er tilfelle.
    // https://stackoverflow.com/questions/19987428/jax-rs-application-subclass-injection
    private ApiConfig getApiConfig() {
        if(this.application instanceof ApiConfig) {
            return (ApiConfig) this.application;
        }
        if(this.application instanceof ResourceConfig) {
            final ResourceConfig rc = (ResourceConfig)this.application;
            if(rc.getApplication() instanceof ApiConfig) {
                return (ApiConfig) rc.getApplication();
            }
        }
        throw new RuntimeException("Application must be instance of ApiConfig (that has getResolvedOpenAPI() method)");
    }

    @PostConstruct
    public void init() {
        super.setResolvedOpenAPI((this.getApiConfig().getResolvedOpenAPI()));
    }
}
