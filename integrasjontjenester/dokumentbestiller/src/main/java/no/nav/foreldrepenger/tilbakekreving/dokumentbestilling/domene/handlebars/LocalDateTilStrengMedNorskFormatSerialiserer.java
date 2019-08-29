package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars;


import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateTilStrengMedNorskFormatSerialiserer extends JsonSerializer {

    private static final DateFormat FORMATTER = DateFormat.getDateInstance(1, new Locale("no"));

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof LocalDate) {
            LocalDate verdi = (LocalDate) o;
            jsonGenerator.writeObject(FORMATTER.format(Date.valueOf(verdi)));
        } else {
            throw new IllegalArgumentException("Serialiserer kan bare brukes for LocalDate");
        }

    }
}
