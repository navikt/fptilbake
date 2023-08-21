package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import java.net.URISyntaxException;
import java.util.List;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.app.IndexClasses;

@Provider
public class JacksonJsonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    /**
     * Default instance for Jax-rs application. Vil på sikt serializere Kodeverdi som ren String basert på kode
     * Foreløpig er
     */
    public JacksonJsonConfig() {
        this(!Fagsystem.FPTILBAKE.equals(ApplicationName.hvilkenTilbake()));
    }

    /**
     * Brukes både i generell serialisering og til å lage en map av kodeverk (serialisert som objekt)
     */
    public JacksonJsonConfig(boolean serialiserKodeverdiSomObjekt) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(createModule(serialiserKodeverdiSomObjekt));

        objectMapper.registerSubtypes(getJsonTypeNameClasses());

    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private static SimpleModule createModule(boolean serialiserKodeverdiSomObjekt) {
        SimpleModule module = new SimpleModule("VL-REST_MED_INNTEKTSMELDING", new Version(1, 0, 0, null, null, null));

        addSerializers(module, serialiserKodeverdiSomObjekt);
        return module;
    }

    private static void addSerializers(SimpleModule module, boolean serialiserKodeverdiSomObjekt) {
        module.addSerializer(new KodelisteSerializer(serialiserKodeverdiSomObjekt));
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

    /**
     * Scan subtyper dynamisk fra WAR slik at superklasse slipper å deklarere @JsonSubtypes.
     */
    public static List<Class<?>> getJsonTypeNameClasses() {
        Class<JacksonJsonConfig> cls = JacksonJsonConfig.class;
        IndexClasses indexClasses;
        try {
            indexClasses = IndexClasses.getIndexFor(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
            return indexClasses.getClassesWithAnnotation(JsonTypeName.class);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Kunne ikke konvertere CodeSource location til URI", e);
        }
    }
}
