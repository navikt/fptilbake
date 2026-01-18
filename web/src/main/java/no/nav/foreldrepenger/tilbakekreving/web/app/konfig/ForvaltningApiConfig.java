package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningAktørRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningBehandlingRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.ForvaltningKravgrunnlagRestTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.JettyServer;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ForvaltningApiConfig.API_URI)
public class ForvaltningApiConfig extends Application {

    private static final Fagsystem HVILKEN_TILBAKE = ApplicationName.hvilkenTilbake();

    public static final String API_URI = "/forvaltning/api";

    public ForvaltningApiConfig() {
        if (Fagsystem.FPTILBAKE.equals(HVILKEN_TILBAKE)) {
            setupOpenAPI();
        }
    }

    private void setupOpenAPI() {
        final var info = new Info()
            .title("Vedtaksløsningen - Tilbakekreving")
            .version("1.1")
            .description("REST grensesnitt for tilbakekreving.");
        final var server = new Server().url(JettyServer.getContextPath());
        var oas = new OpenAPI()
            .openapi("3.1.1")
            .info(info)
            .addServersItem(server);
        var swaggerConfiguration = new SwaggerConfiguration()
            .id(idFra(this))
            .openAPI(oas)
            .prettyPrint(true)
            .scannerClass(JaxrsAnnotationScanner.class.getName())
            .resourceClasses(getForvaltningClasses().stream().map(Class::getName).collect(Collectors.toSet()));
        try {
            new JaxrsOpenApiContextBuilder<>()
                .ctxId(idFra(this))
                .application(this)
                .openApiConfiguration(swaggerConfiguration)
                .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    private static String idFra(Application application) {
        return "openapi.context.id.servlet." + application.getClass().getName();
    }

    @Override
    public Set<Class<?>> getClasses() {
        if (!Fagsystem.FPTILBAKE.equals(HVILKEN_TILBAKE)) {
            return Collections.emptySet();
        }
        var classes = new HashSet<>(getForvaltningClasses());
        classes.addAll(Set.of(
            // autentisering etter ny standard
            AuthenticationFilter.class,
            ForvaltningAuthorizationFilter.class,
            // Applikasjonsoppsett
            JacksonJsonConfig.class,
            // ExceptionMappers pga de som finnes i Jackson+Jersey-media
            ConstraintViolationMapper.class,
            JsonMappingExceptionMapper.class,
            JsonParseExceptionMapper.class,
            // Generell exceptionmapper m/logging for øvrige tilfelle
            GeneralRestExceptionMapper.class));

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
