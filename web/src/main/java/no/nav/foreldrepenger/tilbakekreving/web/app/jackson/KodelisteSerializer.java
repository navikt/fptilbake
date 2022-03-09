package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

/**
 * Enkel serialisering av KodeverkTabell klasser, uten at disse trenger @JsonIgnore eller lignende. Deserialisering går
 * av seg selv normalt (får null for andre felter).
 */
public class KodelisteSerializer extends StdSerializer<Kodeverdi> {

    private boolean serialiserKodeverdiSomObjekt;

    public KodelisteSerializer(boolean serialiserKodeverdiSomObjekt) {
        super(Kodeverdi.class);
        this.serialiserKodeverdiSomObjekt = serialiserKodeverdiSomObjekt;
    }

    @Override
    public void serialize(Kodeverdi value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

        if (!serialiserKodeverdiSomObjekt) {
            jgen.writeString(value.getKode());
            return;
        }

        jgen.writeStartObject();

        jgen.writeStringField("kode", value.getKode());
        jgen.writeStringField("navn", value.getNavn());
        jgen.writeStringField("kodeverk", value.getKodeverk());

        jgen.writeEndObject();
    }

}
