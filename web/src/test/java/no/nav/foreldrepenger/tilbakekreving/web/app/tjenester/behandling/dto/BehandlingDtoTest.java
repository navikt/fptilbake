package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class BehandlingDtoTest {
    @Test
    void sjekk_at_begge_det_finnes_keys_med_og_uten_æøå() {
        var b = new BehandlingDto();
        b.setSpråkkode(Språkkode.NB);
        b.setVenteÅrsakKode(Venteårsak.AVVENTER_DOKUMENTASJON.getKode());
        b.setFristBehandlingPåVent(LocalDateTime.now().plusWeeks(1).toString());
        b.setBehandlingKøet(false);
        var s = DefaultJsonMapper.toJson(b);
        assertThat(s)
                .contains("språkkode", "sprakkode")
                .contains("behandlingKøet", "behandlingKoet")
                .contains("venteÅrsakKode", "venteArsakKode")
                .contains("behandlingPåVent", "behandlingPaaVent")
                .contains("fristBehandlingPåVent", "fristBehandlingPaaVent");
    }
}
