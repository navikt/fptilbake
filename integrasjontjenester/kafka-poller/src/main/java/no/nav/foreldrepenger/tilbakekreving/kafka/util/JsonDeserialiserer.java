package no.nav.foreldrepenger.tilbakekreving.kafka.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonDeserialiserer {
    private static final ObjectMapper OM;

    static {
        OM = new ObjectMapper();
        OM.registerModule(new JavaTimeModule());
        OM.registerModule(new Jdk8Module());
        OM.registerModule(new SimpleModule());
    }

    private JsonDeserialiserer() {
        //hindrer instansiering, dette gjør SonarQube glad
    }

    public static <T> T deserialiser(String melding, Class<T> klassetype) {
        try {
            return OM.readValue(melding, klassetype);
        } catch (IOException e) {
            throw new IllegalArgumentException("Kunne ikke deserialisere basert på Objektet med klassetype " + klassetype.getName() + " melding: " + melding, e);
        }
    }

}
