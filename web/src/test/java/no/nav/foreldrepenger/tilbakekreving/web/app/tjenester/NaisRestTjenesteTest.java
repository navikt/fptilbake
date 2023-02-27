package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.Selftests;

class NaisRestTjenesteTest {

    private NaisRestTjeneste restTjeneste;

    private final Selftests selftestsMock = mock(Selftests.class);

    @BeforeEach
    public void setup() {
        restTjeneste = new NaisRestTjeneste(selftestsMock);
    }

    @Test
    void test_isAlive_skal_returnere_status_200() {
        when(selftestsMock.isReady()).thenReturn(true);
        when(selftestsMock.isKafkaAlive()).thenReturn(true);

        Response response = restTjeneste.isAlive();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    void test_isReady_skal_returnere_service_unavailable_når_kritiske_selftester_feiler() {
        when(selftestsMock.isReady()).thenReturn(false);

        Response response = restTjeneste.isReady();

        assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    void test_isReady_skal_returnere_status_ok_når_selftester_er_ok() {
        when(selftestsMock.isReady()).thenReturn(true);

        Response response = restTjeneste.isReady();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

}
