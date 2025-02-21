package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.openapi.spec.utils.jackson.DynamicObjectMapperResolver;
import no.nav.openapi.spec.utils.jackson.OpenapiCompatObjectMapperModifier;

public class JacksonJsonConfig extends DynamicObjectMapperResolver {
    private final String JSON_SERIALIZER_KODEVERDI_SOM_OBJEKT = "kodeverdi-objekt";
    private final String JSON_SERIALIZER_KODEVERDI_SOM_STRING = "kodeverdi-string";

    private static ObjectMapper defaultObjectMapper() {
        final boolean serialiserKodeverdiSomObjekt = !Fagsystem.FPTILBAKE.equals(ApplicationName.hvilkenTilbake());
        return ObjectMapperFactory.getDefaultObjectMapperCopy(serialiserKodeverdiSomObjekt);
    }

    public JacksonJsonConfig() {
        super(defaultObjectMapper());
        final ObjectMapper openapiObjectMapper = OpenapiCompatObjectMapperModifier
            .withDefaultModifications()
            .modify(ObjectMapperFactory.getBaseObjectMapperCopy());
        super.addObjectMapper(DynamicObjectMapperResolver.JSON_SERIALIZER_OPENAPI, openapiObjectMapper);
        final ObjectMapper kodeverdiObjektMapper = ObjectMapperFactory.getDefaultObjectMapperCopy(true);
        super.addObjectMapper(JSON_SERIALIZER_KODEVERDI_SOM_OBJEKT, kodeverdiObjektMapper);
        final ObjectMapper kodeverdiStringMapper = ObjectMapperFactory.getDefaultObjectMapperCopy(false);
        super.addObjectMapper(JSON_SERIALIZER_KODEVERDI_SOM_STRING, kodeverdiStringMapper);
    }
}
