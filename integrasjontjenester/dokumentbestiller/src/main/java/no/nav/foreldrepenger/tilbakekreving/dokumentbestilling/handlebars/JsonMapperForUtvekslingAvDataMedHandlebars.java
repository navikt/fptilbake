package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import tools.jackson.databind.json.JsonMapper;

public class JsonMapperForUtvekslingAvDataMedHandlebars {

    // En hel del is-gettere som er utledninger fra andre felt og som ikke er annotert med JProperty
    // Pluss div andre avvik som gjør at DefaultJsonMapper ikke fungerer ut av boksen
    public static final JsonMapper INSTANCE = JsonMapper.builder()
        .changeDefaultVisibility(v -> v
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withCreatorVisibility(JsonAutoDetect.Visibility.ANY)
            .withScalarConstructorVisibility(JsonAutoDetect.Visibility.ANY))
        .build();

}
