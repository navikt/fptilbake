package no.nav.foreldrepenger.tilbakekreving.sokos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class SokosDtoTest {

    @Test
    void vedtak() {
        assertJsonRoundtrip("""
            {
              "kodeAksjon": "8", "vedtakId": 892793, "vedtaksDato": "2026-01-15",
              "kodeHjemmel": "22-15", "enhetAnsvarlig": "8020",
              "kontrollfelt": "2026-01-15-01.12.34.123456", "saksbehandlerId": "Z999999",
              "datoTilleggsfrist": "2026-03-15",
              "perioder": [{
                "periodeFom": "2026-01-01", "periodeTom": "2026-01-31",
                "belopRenter": 150.50,
                "posteringer": [{
                  "kodeKlasse": "SPATORD", "belopOpprinneligUtbetalt": 10000.00,
                  "belopNy": 8000.00, "belopTilbakekreves": 2000.00,
                  "belopUinnkrevd": 0, "belopSkatt": 500.00,
                  "kodeResultat": "FULL_TILBAKEKREV", "kodeAarsak": "ANNET", "kodeSkyld": "NAV"
                }]
              }]
            }
            """, TilbakekrevingsvedtakRequest.class);
        assertJsonRoundtrip("""
            {"status": 0, "melding": "OK", "vedtakId": 892793, "datoVedtakFagsystem": "2026-01-15"}
            """, TilbakekrevingsvedtakResponse.class);
    }

    @Test
    void kravgrunnlagDetaljer() {
        assertJsonRoundtrip("""
            {"kodeAksjon": "5", "kravgrunnlagId": 570598, "enhetAnsvarlig": "4819", "saksbehandlerId": "Z999999"}
            """, HentKravgrunnlagDetaljerRequest.class);
        assertJsonRoundtrip("""
            {
              "status": 0, "melding": "OK", "kravgrunnlag": {
                "kravgrunnlagId": 123456, "vedtakId": 123456789, "kodeStatusKrav": "NY",
                "kodeFagomraade": "FP", "fagsystemId": "FS123456",
                "datoVedtakFagsystem": "2026-01-15", "vedtakIdOmgjort": 123456788,
                "gjelderId": "12345678901", "typeGjelder": "PERSON",
                "utbetalesTilId": "12345678901", "typeUtbetalesTilId": "PERSON",
                "kodeHjemmel": "FVL-22", "renterBeregnes": true,
                "enhetAnsvarlig": "8020", "enhetBosted": "0219", "enhetBehandl": "8020",
                "kontrollfelt": "2026-01-15-01.12.34.123456", "saksbehandlerId": "Z999999",
                "referanse": "REF123456", "datoTilleggsfrist": "2026-03-15",
                "perioder": [{
                  "periodeFom": "2026-01-01", "periodeTom": "2026-01-31",
                  "belopSkattMnd": 500.00, "posteringer": [{
                    "kodeKlasse": "KL_KODE_CLASS1", "typeKlasse": "YTEL",
                    "belopOpprinneligUtbetalt": 10000.00, "belopNy": 8000.00,
                    "belopTilbakekreves": 2000.00, "belopUinnkrevd": 0.00,
                    "skattProsent": 25.0, "kodeResultat": "FULL_TILBAKEKREV",
                    "kodeAarsak": "FEIL_OPPLYSNINGER", "kodeSkyld": "BRUKER"
                  }]
                }]
              }
            }
            """, HentKravgrunnlagDetaljerResponse.class);
    }

    @Test
    void annulerOgFeil() {
        assertJsonRoundtrip("""
            {"kodeAksjon": "A", "vedtakId": 892793, "enhetAnsvarlig": "8020", "saksbehandlerId": "Z999999"}
            """, KravgrunnlagAnnulerRequest.class);
        assertJsonRoundtrip("""
            {"status": 0, "melding": "OK", "vedtakId": 892793, "saksbehandlerId": "Z999999"}
            """, KravgrunnlagAnnulerResponse.class);
        var errorJson = """
            {
              "timestamp": "2026-01-15T12:34:56Z", "status": 500,
              "error": "Internal Server Error", "message": "Det skjedde en feil",
              "path": "/api/v1/tilbakekreving/vedtak"
            }
            """;
        var error = DefaultJsonMapper.fromJson(errorJson, ApiError.class);
        var serializedError = DefaultJsonMapper.fromJson(DefaultJsonMapper.toJson(error), ApiError.class);
        assertThat(serializedError.timestamp().toInstant()).isEqualTo(error.timestamp().toInstant());
        assertThat(serializedError.status()).isEqualTo(error.status());
        assertThat(serializedError.error()).isEqualTo(error.error());
        assertThat(serializedError.message()).isEqualTo(error.message());
        assertThat(serializedError.path()).isEqualTo(error.path());
    }

    private static <T> void assertJsonRoundtrip(String json, Class<T> type) {
        var dto = DefaultJsonMapper.fromJson(json, type);
        var serialized = DefaultJsonMapper.toJson(dto);
        assertThat(DefaultJsonMapper.fromJson(serialized, Map.class))
            .isEqualTo(DefaultJsonMapper.fromJson(json, Map.class));
    }
}
