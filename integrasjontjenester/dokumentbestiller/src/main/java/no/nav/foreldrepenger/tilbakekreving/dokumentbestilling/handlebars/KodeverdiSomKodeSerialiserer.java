package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;


import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class KodeverdiSomKodeSerialiserer extends StdSerializer<Kodeverdi> {

    public KodeverdiSomKodeSerialiserer() {
        super(Kodeverdi.class);
    }

    @Override
    public void serialize(Kodeverdi o, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        if (o != null) {
            jsonGenerator.writePOJO(o.getKode());
        }
    }
}
