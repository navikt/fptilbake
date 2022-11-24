package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.Selftests;

public class HealthCheckRestServiceTest {

    private HealthCheckRestService restTjeneste;

    private final Selftests selftestsMock = mock(Selftests.class);

    @BeforeEach
    public void setup() {
        restTjeneste = new HealthCheckRestService(selftestsMock);
    }

    @Test
    public void test_isAlive_skal_returnere_status_200() {
        when(selftestsMock.isReady()).thenReturn(true);
        when(selftestsMock.isKafkaAlive()).thenReturn(true);
        restTjeneste.setIsContextStartupReady(true);

        Response response = restTjeneste.isAlive();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void test_isReady_skal_returnere_service_unavailable_når_kritiske_selftester_feiler() {
        when(selftestsMock.isReady()).thenReturn(false);
        restTjeneste.setIsContextStartupReady(true);

        Response response = restTjeneste.isReady();

        assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void test_isReady_skal_returnere_status_ok_når_selftester_er_ok() {
        when(selftestsMock.isReady()).thenReturn(true);
        restTjeneste.setIsContextStartupReady(true);

        Response response = restTjeneste.isReady();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

}
