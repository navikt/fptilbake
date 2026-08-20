package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import no.nav.foreldrepenger.tilbakekreving.datavarehus.felles.JsonObjectMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;
import tools.jackson.core.JacksonException;

public class VedtakOppsummeringMapper {

    private VedtakOppsummeringMapper() {
    }

    public static String tilJsonString(VedtakOppsummering verdi) {
        try {
            return JsonObjectMapper.OM.writeValueAsString(verdi);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Klarte ikke serialisere til string", e);
        }
    }

}
