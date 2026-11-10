package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

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
    public void serialize(Kodeverdi value, JsonGenerator jgen, SerializationContext ctxt) {

        if (!serialiserKodeverdiSomObjekt) {
            jgen.writeString(value.getKode());
            return;
        }

        jgen.writeStartObject();

        jgen.writeStringProperty("kode", value.getKode());
        jgen.writeStringProperty("navn", value.getNavn());
        jgen.writeStringProperty("kodeverk", value.getKodeverk());

        jgen.writeEndObject();
    }

}
