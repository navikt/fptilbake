package no.nav.foreldrepenger.tilbakekreving.datavarehus.felles;

import java.time.ZoneId;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public class JsonObjectMapper {

    // Mysterie rundt bruk av UTC. Mulig DVH er OK med Europe/Oslo som gir Thh:mm:ss:001+01:00 i stedet for Thh:mm:ss:001Z
    public static final ObjectMapper OM = DefaultJsonMapper.getJsonMapper()
        .rebuild()
        .defaultTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")))
        .build();
}
