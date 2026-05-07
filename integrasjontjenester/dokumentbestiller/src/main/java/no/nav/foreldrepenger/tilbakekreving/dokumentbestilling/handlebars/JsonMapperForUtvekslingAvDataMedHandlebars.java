package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonMapperForUtvekslingAvDataMedHandlebars {

    // En hel del is-gettere som er utledninger fra andre felt og som ikke er annotert med JProperty
    // Pluss div andre avvik som gjør at DefaultJsonMapper ikke fungerer ut av boksen
    public static final JsonMapper INSTANCE = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .addModule(new Jdk8Module())
        .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
        .visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
        .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .visibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY)
        .build();

}
