package no.nav.foreldrepenger.tilbakekreving.sokos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;

class SokosTilbakekrevingKlientTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void godtar_kvittering_som_er_ok_eller_ok_med_merknader(int status) {
        assertThatCode(() -> SokosTilbakekrevingKlient.validerKvittering(kvittering(status)))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 16})
    void avviser_kvittering_med_feil(int status) {
        assertThatThrownBy(() -> SokosTilbakekrevingKlient.validerKvittering(kvittering(status)))
            .isInstanceOf(IntegrasjonException.class)
            .hasMessageContaining("status " + status);
    }

    @Test
    void avviser_manglende_status() {
        assertThatThrownBy(() -> SokosTilbakekrevingKlient.validerKvittering(kvittering(null)))
            .isInstanceOf(IntegrasjonException.class)
            .hasMessageContaining("status null");
    }

    private static TilbakekrevingsvedtakResponse kvittering(Integer status) {
        return new TilbakekrevingsvedtakResponse(status, "Melding", 123, null);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void godtar_annullering_som_er_ok_eller_ok_med_merknader(int status) {
        assertThatCode(() -> SokosTilbakekrevingKlient.validerAnnullering(annullerKvittering(status)))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12, 16})
    void avviser_annullering_med_feil(int status) {
        assertThatThrownBy(() -> SokosTilbakekrevingKlient.validerAnnullering(annullerKvittering(status)))
            .isInstanceOf(IntegrasjonException.class)
            .hasMessageContaining("status " + status);
    }

    @Test
    void avviser_annullering_uten_status() {
        assertThatThrownBy(() -> SokosTilbakekrevingKlient.validerAnnullering(annullerKvittering(null)))
            .isInstanceOf(IntegrasjonException.class)
            .hasMessageContaining("status null");
    }

    private static KravgrunnlagAnnulerResponse annullerKvittering(Integer status) {
        return new KravgrunnlagAnnulerResponse(status, "Melding", 123, "K231B433");
    }

    @Test
    void bevarer_melding_fra_sokos_ved_http_400() {
        var respons = httpRespons(400, """
            {
              "timestamp": "2026-01-15T12:34:56Z", "status": 400,
              "error": "Bad Request", "message": "B420010I", "path": "/kravgrunnlag/detaljer"
            }
            """);

        assertThatThrownBy(() -> SokosTilbakekrevingKlient.handleKravgrunnlagResponse(respons))
            .isInstanceOfSatisfying(SokosKravgrunnlagException.class,
                feil -> assertThat(feil.melding()).isEqualTo("B420010I"));
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void skiller_autorisasjonsfeil_ved_henting(int status) {
        assertThatThrownBy(() -> SokosTilbakekrevingKlient.handleKravgrunnlagResponse(httpRespons(status, "")))
            .isInstanceOf(ManglerTilgangException.class);
    }

    @Test
    void avviser_annen_http_feil_ved_henting() {
        assertThatThrownBy(() -> SokosTilbakekrevingKlient.handleKravgrunnlagResponse(httpRespons(500, "")))
            .isInstanceOf(IntegrasjonException.class);
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> httpRespons(int status, String body) {
        var respons = (HttpResponse<String>) mock(HttpResponse.class);
        when(respons.statusCode()).thenReturn(status);
        when(respons.body()).thenReturn(body);
        return respons;
    }

}
