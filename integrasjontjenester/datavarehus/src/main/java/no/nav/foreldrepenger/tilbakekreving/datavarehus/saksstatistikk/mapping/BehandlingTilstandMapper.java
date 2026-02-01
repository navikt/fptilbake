package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public class BehandlingTilstandMapper {

    public static BehandlingTilstand fraJson(String json) {
        return DefaultJsonMapper.fromJson(json, BehandlingTilstand.class);
    }

    public static String tilJsonString(BehandlingTilstand verdi) {
        return DefaultJsonMapper.toJson(verdi);
    }

}
