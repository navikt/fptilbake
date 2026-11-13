package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import java.net.URISyntaxException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.web.app.IndexClasses;
import no.nav.openapi.spec.utils.jackson.JsonParserPreProcessingDeserializerModifier;
import no.nav.openapi.spec.utils.jackson.ObjectToPropertyPreProcessor;
import no.nav.openapi.spec.utils.jackson.OpenapiCompatObjectMapperModifier;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * JsonMapper oppsett for K9-tilbake. Basert på Jackson 2 standardoppsett (builderWithJackson2Defaults) for å få
 * samme serialisering/deserialisering som før overgang til Jackson 3.
 */
public class ObjectMapperFactory {

    private ObjectMapperFactory() {
    }

    public static JsonMapper createBaseObjectMapper() {
        return JsonMapper.builderWithJackson2Defaults()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .registerSubtypes(getJsonTypeNameClasses())
            .build();
    }

    private static final class BaseObjectMapperHolder {
        private static final JsonMapper BASE_OBJECT_MAPPER = createBaseObjectMapper();
    }

    /**
     * JsonMapper er immutable, så samme instans kan delast. Bruk rebuild() for å lage modifisert variant.
     */
    public static JsonMapper getBaseObjectMapper() {
        return BaseObjectMapperHolder.BASE_OBJECT_MAPPER;
    }

    public static JsonMapper getDefaultObjectMapper(final boolean serialiserKodeverdiSomObjekt) {
        return getBaseObjectMapper().rebuild()
            .addModule(createOverstyrendeKodelisteSerializerModule(serialiserKodeverdiSomObjekt))
            .build();
    }

    public static JsonMapper getOpenapiObjectMapper() {
        return OpenapiCompatObjectMapperModifier
            .withDefaultModifications()
            .modify(getBaseObjectMapper());
    }

    /**
     * Scan subtyper dynamisk fra WAR slik at superklasse slipper å deklarere @JsonSubtypes.
     */
    public static List<Class<?>> getJsonTypeNameClasses() {
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
