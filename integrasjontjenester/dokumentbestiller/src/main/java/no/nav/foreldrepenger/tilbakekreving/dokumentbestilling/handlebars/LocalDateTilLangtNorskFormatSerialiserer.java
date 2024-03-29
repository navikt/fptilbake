package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;


import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateTilLangtNorskFormatSerialiserer extends JsonSerializer<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d. MMMM yyyy", new Locale("no"));

    @Override
    public void serialize(LocalDate o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o != null) {
            jsonGenerator.writeObject(FORMATTER.format(o));
        }
    }
}
