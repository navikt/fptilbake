package no.nav.foreldrepenger.tilbakekreving.web.app.util;

import java.io.IOException;

import io.swagger.v3.oas.models.OpenAPI;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.ApiConfig;
import no.nav.openapi.spec.utils.openapi.FileOutputter;

public class OpenapiGenerate {

    public static void main(String[] args) throws IOException {
        final ApiConfig apiConfig = new ApiConfig();
        final OpenAPI resolved = apiConfig.getResolvedOpenAPI();
        final var outputPath = args.length > 0 ? args[0] : "";
        FileOutputter.writeJsonFile(resolved, outputPath);
    }
}
