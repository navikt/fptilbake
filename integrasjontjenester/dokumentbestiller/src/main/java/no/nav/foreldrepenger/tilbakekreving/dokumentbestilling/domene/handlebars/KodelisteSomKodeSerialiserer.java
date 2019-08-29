package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars;


import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

public class KodelisteSomKodeSerialiserer extends JsonSerializer {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof Kodeliste) {
            Kodeliste verdi = (Kodeliste) o;
            jsonGenerator.writeObject(verdi.getKode());
        } else {
            throw new IllegalArgumentException("Serialiserer kan bare brukes for Kodeliste");
        }
    }
}
