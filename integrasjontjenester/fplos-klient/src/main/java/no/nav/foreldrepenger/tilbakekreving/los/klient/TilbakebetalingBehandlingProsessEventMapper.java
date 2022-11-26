package no.nav.foreldrepenger.tilbakekreving.fplos.klient;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;

public class TilbakebetalingBehandlingProsessEventMapper {

    private static final ObjectMapper OBJECT_MAPPER = lagObjectMapper();

    private TilbakebetalingBehandlingProsessEventMapper() {
    }

    public static String getJson(TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto) throws IOException {
        Writer jsonWriter = new StringWriter();
        OBJECT_MAPPER.writeValue(jsonWriter, behandlingProsessEventDto);
        jsonWriter.flush();
        return jsonWriter.toString();
    }

    private static ObjectMapper lagObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.registerModule(new Jdk8Module());
        return om;
    }
}
