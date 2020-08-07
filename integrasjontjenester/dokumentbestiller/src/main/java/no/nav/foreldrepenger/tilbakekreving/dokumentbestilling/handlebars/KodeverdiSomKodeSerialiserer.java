package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;


import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public class KodeverdiSomKodeSerialiserer extends JsonSerializer {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof Kodeverdi) {
            Kodeverdi verdi = (Kodeverdi) o;
            jsonGenerator.writeObject(verdi.getKode());
        } else {
            throw new IllegalArgumentException("Serialiserer kan bare brukes for Kodeverdi");
        }
    }
}
