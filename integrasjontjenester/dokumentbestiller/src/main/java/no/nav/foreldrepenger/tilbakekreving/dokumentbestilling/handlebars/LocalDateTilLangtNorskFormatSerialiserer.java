package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class LocalDateTilLangtNorskFormatSerialiserer extends StdSerializer<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d. MMMM yyyy", new Locale("no"));

    public LocalDateTilLangtNorskFormatSerialiserer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate o, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        if (o != null) {
            jsonGenerator.writePOJO(FORMATTER.format(o));
        }
    }
}
