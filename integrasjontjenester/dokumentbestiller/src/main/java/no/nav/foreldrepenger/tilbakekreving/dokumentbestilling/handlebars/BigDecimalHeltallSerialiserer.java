package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;


import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * bruker for å unngå at runde tall blir blir presentert som eks. 1E5 istedet for 10000.
 * <p>
 * TODO: bør bruke noe i handlebars for å håndtere formattering, decorator? formatter+
 */
public class BigDecimalHeltallSerialiserer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o != null) {
            jsonGenerator.writeObject(o.longValueExact());
        }
    }
}
