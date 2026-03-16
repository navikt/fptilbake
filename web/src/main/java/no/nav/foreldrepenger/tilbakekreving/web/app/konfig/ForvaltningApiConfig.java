package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningAktørRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningBehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningKravgrunnlagRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.JettyServer;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;
import no.nav.vedtak.openapi.OpenApiUtils;
import no.nav.vedtak.server.rest.ForvaltningAuthorizationFilter;
import no.nav.vedtak.server.rest.FpRestJackson2Feature;

@ApplicationPath(ForvaltningApiConfig.API_URI)
public class ForvaltningApiConfig extends Application {

    private static final Fagsystem HVILKEN_TILBAKE = ApplicationName.hvilkenTilbake();

    public static final String API_URI = "/forvaltning/api";

    public ForvaltningApiConfig() {
        if (Fagsystem.FPTILBAKE.equals(HVILKEN_TILBAKE)) {
            OpenApiUtils.setupOpenApi("Forvaltning - Tilbakekreving", JettyServer.getContextPath(),
                getForvaltningClasses(), this);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        if (!Fagsystem.FPTILBAKE.equals(HVILKEN_TILBAKE)) {
            return Collections.emptySet();
        }
        var classes = new HashSet<>(getForvaltningClasses());
        classes.add(FpRestJackson2Feature.class);
        classes.add(ForvaltningAuthorizationFilter.class);
        // swagger
        classes.add(OpenApiResource.class);

        return Collections.unmodifiableSet(classes);
    }

    private static Set<Class<?>> getForvaltningClasses() {
        return Set.of(ProsessTaskRestTjeneste.class,
            ForvaltningAktørRestTjeneste.class,
            ForvaltningBehandlingRestTjeneste.class,
            ForvaltningKravgrunnlagRestTjeneste.class);
    }

    @Override
    public Map<String, Object> getProperties() {
        if (!Fagsystem.FPTILBAKE.equals(HVILKEN_TILBAKE)) {
            return Collections.emptyMap();
        }
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }
}
