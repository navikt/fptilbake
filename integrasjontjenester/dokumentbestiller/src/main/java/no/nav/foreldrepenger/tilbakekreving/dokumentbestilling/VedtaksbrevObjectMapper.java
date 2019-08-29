package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class VedtaksbrevObjectMapper {

    public static final ObjectMapper INSTANCE;
    static {
        INSTANCE = new ObjectMapper();
        INSTANCE.registerModule(new JavaTimeModule());
        INSTANCE.registerModule(new Jdk8Module());
        INSTANCE.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        INSTANCE.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        INSTANCE.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        INSTANCE.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
    }

}
