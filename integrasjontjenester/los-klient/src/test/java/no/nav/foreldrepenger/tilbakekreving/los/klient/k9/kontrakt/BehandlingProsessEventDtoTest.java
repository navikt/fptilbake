package no.nav.foreldrepenger.tilbakekreving.los.klient.k9.kontrakt;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;


class BehandlingProsessEventDtoTest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @Test
    void testAksjonspunktEventRoundtrip() {
        Map<String, String> aksjonspunkter = new HashMap<>();
        aksjonspunkter.put("5080", "OPPR");

        BehandlingProsessEventDto baseDto = BehandlingProsessEventDto.builder()
                .medFagsystem("K9")
                .medAktørId("123457890123")
                .medSaksnummer("9876543210")
                .medYtelseTypeKode("PSB")
                .medBehandlingTypeKode("BT-004")
                .medEventTid(LocalDateTime.now())
                .medOpprettetBehandling(LocalDateTime.now().minusHours(10))
                .medEksternId(UUID.randomUUID())
                .medAksjonspunktKoderMedStatusListe(aksjonspunkter)
                .medBehandlendeEnhet("4803")
                .build();

        TilbakebetalingBehandlingProsessEventDto tilbakebetalingDto = TilbakebetalingBehandlingProsessEventDto.builder()
                .medFagsystem("K9TILBAKE")
                .medFeilutbetaltBeløp(BigDecimal.valueOf(20000L))
                .medFørsteFeilutbetaling(LocalDate.now().minusMonths(4))
                .medAnsvarligSaksbehandlerIdent("T12345")
                .medHref("http://tilbakekreving/")
                .medAktørId("123457890123")
                .medSaksnummer("9876543210")
                .medYtelseTypeKode("PSB")
                .medBehandlingTypeKode("BT-004")
                .medEventTid(LocalDateTime.now())
                .medOpprettetBehandling(LocalDateTime.now().minusHours(10))
                .medEksternId(UUID.randomUUID())
                .medAksjonspunktKoderMedStatusListe(aksjonspunkter)
                .medBehandlendeEnhet("4803")
                .build();

        testRoundtrip(tilbakebetalingDto, TilbakebetalingBehandlingProsessEventDto.class);
        testRoundtrip(baseDto, BehandlingProsessEventDto.class);
    }

    private static <T> T testRoundtrip(BehandlingProsessEventDto dto, Class<T> cls) {
        String json = serialiserToJson(dto);
        var roundtrippedDto = deserialiser(json, BehandlingProsessEventDto.class);
        assertThat(roundtrippedDto).isInstanceOf(cls);
        return null;
    }

    private static String serialiserToJson(Object objekt)  {
        return JSON_MAPPER.writeValueAsString(objekt);
    }

    private static <T> T deserialiser(String melding, Class<T> klassetype) {
        var mapper = JSON_MAPPER.rebuild().addMixIn(BehandlingProsessEventDto.class, BehandlingProsessEventMixin.class).build();
        return mapper.readValue(melding, klassetype);
    }

}
