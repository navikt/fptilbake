package no.nav.foreldrepenger.tilbakekreving.sensu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;

class SensuEventTest {

    @BeforeEach
    void setup() {
        ApplicationName.clearAppName();
        System.setProperty("app.name", "k9-tilbake");
    }

    @AfterEach
    void teardown() {
        System.clearProperty("app.name");
        ApplicationName.clearAppName();

    }

    @Test
    void toSensuRequest_illegal_state_exception_metrikk_felter_kan_ikke_være_tomt() {
        assertThrows(IllegalStateException.class, () -> SensuEvent.createSensuEvent("testMetric", Map.of()).toSensuRequest());
    }

    @Test
    void toSensuRequest() {
        final SensuEvent data = SensuEvent.createSensuEvent("testMetric", Map.of("test", 1));
        final SensuEvent.SensuRequest sensuRequest = data.toSensuRequest();

        assertThat(sensuRequest).isNotNull();
        String jsonRequest = sensuRequest.toJson();
        assertThat(jsonRequest).contains("test");
        assertThat(jsonRequest).contains("testMetric");
        assertThat(jsonRequest).contains("events_nano");
        assertThat(jsonRequest).contains("metric");
    }

    @Test
    void toSensuRequest_default_tags_er_satt() {
        SensuEvent data = SensuEvent.createSensuEvent("test", Map.of("testMetric", 1));
        final SensuEvent.SensuRequest sensuRequest = data.toSensuRequest();

        String jsonRequest = sensuRequest.toJson();
        assertThat(sensuRequest).isNotNull();
        assertThat(jsonRequest).isNotNull();
        assertThat(jsonRequest).contains("testMetric");
        assertThat(jsonRequest).contains("application");
        assertThat(jsonRequest).contains("cluster");
        assertThat(jsonRequest).contains("namespace");
    }
}
