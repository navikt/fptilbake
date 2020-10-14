package no.nav.journalpostapi.dto.serializer;

import java.io.IOException;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ByteArraySomBase64StringSerializer extends JsonSerializer {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof byte[]) {
            byte[] verdi = (byte[]) o;
            String string = Base64.getEncoder().encodeToString(verdi);
            jsonGenerator.writeString(string);
        } else {
            throw new IllegalArgumentException("Serialiserer kan bare brukes for byte-array");
        }
    }
}
