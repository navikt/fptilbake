package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;


import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public class KodeverdiSomKodeSerialiserer extends JsonSerializer<Kodeverdi> {

    @Override
    public void serialize(Kodeverdi o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o != null) {
            jsonGenerator.writeObject(o.getKode());
        }
    }
}
