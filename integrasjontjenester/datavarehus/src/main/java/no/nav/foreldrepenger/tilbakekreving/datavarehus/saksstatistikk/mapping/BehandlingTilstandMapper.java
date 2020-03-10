package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.tilbakekreving.datavarehus.felles.JsonObjectMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;

public class BehandlingTilstandMapper {

    public static BehandlingTilstand fraJson(String json) {
        try {
            return JsonObjectMapper.OM.readValue(json, BehandlingTilstand.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Klarte ikke parse JSON", e);
        }
    }

    public static String tilJsonString(BehandlingTilstand verdi) {
        try {
            return JsonObjectMapper.OM.writeValueAsString(verdi);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Klarte ikke serialisere til string", e);
        }
    }

}
