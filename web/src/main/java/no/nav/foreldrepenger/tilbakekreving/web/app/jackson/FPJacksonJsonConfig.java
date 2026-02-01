package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import no.nav.foreldrepenger.tilbakekreving.web.app.IndexClasses;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@Provider
public class FPJacksonJsonConfig implements ContextResolver<ObjectMapper> {

    private static final JsonMapper JSON_MAPPER = createObjectMapper();

    private static synchronized JsonMapper createObjectMapper() {
        var typeNameClasses = getJsonTypeNameClasses();
        return DefaultJsonMapper.getJsonMapper().rebuild().registerSubtypes(typeNameClasses).build();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return JSON_MAPPER;
    }

    public static Set<Class<?>> getJsonTypeNameClasses() {
        try {
            var cls = FPJacksonJsonConfig.class;
            var indexClasses = IndexClasses.getIndexFor(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
            return new LinkedHashSet<>(indexClasses.getClassesWithAnnotation(JsonTypeName.class));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Kunne ikke konvertere CodeSource location til URI", e);
        }
    }
}
