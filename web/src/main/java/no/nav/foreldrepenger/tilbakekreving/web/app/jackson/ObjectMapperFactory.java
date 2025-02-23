package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.web.app.IndexClasses;
import no.nav.openapi.spec.utils.jackson.JsonParserPreProcessingDeserializerModifier;
import no.nav.openapi.spec.utils.jackson.ObjectToPropertyPreProcessor;

import java.net.URISyntaxException;
import java.util.List;

public class ObjectMapperFactory {

    public static ObjectMapper createBaseObjectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        objectMapper.registerSubtypes(getJsonTypeNameClasses());
        return objectMapper;
    }

    private static ObjectMapper baseObjectMapper = null;

    public static ObjectMapper getBaseObjectMapperCopy() {
        if(baseObjectMapper == null) {
            baseObjectMapper = createBaseObjectMapper();
        }
        return baseObjectMapper.copy();
    }

    public static ObjectMapper getDefaultObjectMapperCopy(final boolean serialiserKodeverdiSomObjekt) {
        return getBaseObjectMapperCopy()
            .registerModule(createOverstyrendeKodelisteSerializerModule(serialiserKodeverdiSomObjekt));
    }

    /**
     * Scan subtyper dynamisk fra WAR slik at superklasse slipper å deklarere @JsonSubtypes.
     */
    private static List<Class<?>> getJsonTypeNameClasses() {
        Class<ObjectMapperFactory> cls = ObjectMapperFactory.class;
        IndexClasses indexClasses;
        try {
            indexClasses = IndexClasses.getIndexFor(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
            return indexClasses.getClassesWithAnnotation(JsonTypeName.class);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Kunne ikke konvertere CodeSource location til URI", e);
        }
    }

    private static SimpleModule createOverstyrendeKodelisteSerializerModule(boolean serialiserKodeverdiSomObjekt) {
        final SimpleModule module = new SimpleModule("VL-REST_MED_INNTEKTSMELDING", new Version(1, 0, 0, null, null, null));
        module.addSerializer(new KodelisteSerializer(serialiserKodeverdiSomObjekt));
        // Støtt deserialisering frå Kodeverdi serialisert som json objekt utan at typen sjølv støtter det.
        final var kodelisteDeserializerModifier = new JsonParserPreProcessingDeserializerModifier(new ObjectToPropertyPreProcessor(Kodeverdi.class, "kode"));
        module.setDeserializerModifier(kodelisteDeserializerModifier);
        return module;
    }

}
