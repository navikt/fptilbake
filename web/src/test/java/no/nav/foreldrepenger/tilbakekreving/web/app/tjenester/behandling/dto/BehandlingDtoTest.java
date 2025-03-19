package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BehandlingDtoTest {
    @Test
    void sjekk_at_begge_det_finnes_keys_med_og_uten_æøå() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        var s = objectMapper.writeValueAsString(new BehandlingDto());
        assertThat(s).isNotNull();
        assertThat(s).contains("språkkode", "sprakkode");
        assertThat(s).contains("behandlingKøet", "behandlingKoet");
        assertThat(s).contains("venteÅrsakKode", "venteArsakKode");
        assertThat(s).contains("behandlingPåVent", "behandlingPaaVent");
        assertThat(s).contains("fristBehandlingPåVent", "fristBehandlingPaaVent");
    }
}