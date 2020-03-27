package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.tilbakekreving.datavarehus.felles.JsonObjectMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;

public class VedtakOppsummeringMapper {

    public static String tilJsonString(VedtakOppsummering verdi) {
        try {
            return JsonObjectMapper.OM.writeValueAsString(verdi);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Klarte ikke serialisere til string", e);
        }
    }

}
