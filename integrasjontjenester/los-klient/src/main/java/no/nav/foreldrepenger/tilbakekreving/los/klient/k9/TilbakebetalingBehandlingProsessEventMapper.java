package no.nav.foreldrepenger.tilbakekreving.los.klient.k9;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import no.nav.foreldrepenger.tilbakekreving.los.klient.k9.kontrakt.TilbakebetalingBehandlingProsessEventDto;
import tools.jackson.databind.json.JsonMapper;

public class TilbakebetalingBehandlingProsessEventMapper {

    private static final JsonMapper OBJECT_MAPPER = lagObjectMapper();

    private TilbakebetalingBehandlingProsessEventMapper() {
    }

    public static String getJson(TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto) throws IOException {
        Writer jsonWriter = new StringWriter();
        OBJECT_MAPPER.writeValue(jsonWriter, behandlingProsessEventDto);
        jsonWriter.flush();
        return jsonWriter.toString();
    }

    private static JsonMapper lagObjectMapper() {
        return JsonMapper.builder().build();
    }
}
