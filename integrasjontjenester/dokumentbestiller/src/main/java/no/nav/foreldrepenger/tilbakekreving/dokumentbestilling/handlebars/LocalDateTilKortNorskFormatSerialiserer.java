package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;


import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateTilKortNorskFormatSerialiserer extends JsonSerializer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof LocalDate) {
            LocalDate verdi = (LocalDate) o;
            jsonGenerator.writeObject(FORMATTER.format(verdi));
        } else {
            throw new IllegalArgumentException("Serialiserer kan bare brukes for LocalDate");
        }

    }
}
