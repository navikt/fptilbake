package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class LocalDateTilKortNorskFormatSerialiserer extends StdSerializer<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public LocalDateTilKortNorskFormatSerialiserer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate o, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        if (o != null) {
            jsonGenerator.writePOJO(FORMATTER.format(o));
        }
    }
}
