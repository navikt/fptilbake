package no.nav.foreldrepenger.tilbakekreving.web.app.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.v3.core.util.ObjectMapperFactory;
import io.swagger.v3.oas.models.OpenAPI;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.ApiConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenapiGenerate {

    /*
        Instead of using Json class from io.swagger.v3.core.util to serialize to json, we create our own ObjectMapper so
        that we can configure it with a bit of sorting to increase its determinism.

        Without this the output openapi.json would change on each run even though there was no source code changes.
     */
    private static ObjectMapper mapper;

    private static ObjectMapper mapper() {
        if(mapper == null){
            mapper = ObjectMapperFactory.createJson();
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            mapper.configure(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST, true);
            mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        }
        return mapper;
    }

    private static String jsonString(Object o) throws JsonProcessingException {
        final var objWriter = mapper().writer(new DefaultPrettyPrinter());
        return objWriter.writeValueAsString(o);
    }

    public static void main(String[] args) throws IOException {
        final ApiConfig apiConfig = new ApiConfig();
        final OpenAPI resolved = apiConfig.getOpenAPI();
        final String outputJson = jsonString(resolved);

        final var outputPath = args.length > 0 ? args[0] : "";
        if(!outputPath.isEmpty()) {
            if(outputPath.endsWith(".json")) {
                // Ok, write the generated openapi spec to given file path
                final Path path = Paths.get(outputPath);
                Files.writeString(path, outputJson);
            } else {
                throw new RuntimeException("OpenapiGenerate called with invalid outputPath argument ("+ outputPath + ")");
            }
        } else {
            // No outputPath provided, print generated json to stdout
            System.out.println(outputJson);
        }
    }
}
