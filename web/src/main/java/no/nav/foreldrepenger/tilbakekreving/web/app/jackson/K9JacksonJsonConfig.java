package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.openapi.spec.utils.jackson.DynamicObjectMapperResolver;
import tools.jackson.databind.json.JsonMapper;

public class K9JacksonJsonConfig extends DynamicObjectMapperResolver {
    private final String JSON_SERIALIZER_KODEVERDI_SOM_OBJEKT = "kodeverdi-objekt";
    private final String JSON_SERIALIZER_KODEVERDI_SOM_STRING = "kodeverdi-string";

    private static JsonMapper defaultObjectMapper() {
        final boolean serialiserKodeverdiSomObjekt = !Fagsystem.FPTILBAKE.equals(ApplicationName.hvilkenTilbake());
        return ObjectMapperFactory.getDefaultObjectMapper(serialiserKodeverdiSomObjekt);
    }

    public K9JacksonJsonConfig() {
        super(defaultObjectMapper());
        super.addObjectMapper(DynamicObjectMapperResolver.JSON_SERIALIZER_OPENAPI, ObjectMapperFactory.getOpenapiObjectMapper());
        super.addObjectMapper(JSON_SERIALIZER_KODEVERDI_SOM_OBJEKT, ObjectMapperFactory.getDefaultObjectMapper(true));
        super.addObjectMapper(JSON_SERIALIZER_KODEVERDI_SOM_STRING, ObjectMapperFactory.getDefaultObjectMapper(false));
    }
}
