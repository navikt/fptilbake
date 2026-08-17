package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;


import java.math.BigDecimal;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * bruker for å unngå at runde tall blir blir presentert som eks. 1E5 istedet for 10000.
 * <p>
 * TODO: bør bruke noe i handlebars for å håndtere formattering, decorator? formatter+
 */
public class BigDecimalHeltallSerialiserer extends StdSerializer<BigDecimal> {

    public BigDecimalHeltallSerialiserer() {
        super(BigDecimal.class);
    }

    @Override
    public void serialize(BigDecimal o, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        if (o != null) {
            jsonGenerator.writePOJO(o.longValueExact());
        }
    }
}
