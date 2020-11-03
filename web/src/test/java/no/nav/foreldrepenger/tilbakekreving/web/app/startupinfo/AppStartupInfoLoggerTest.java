package no.nav.foreldrepenger.tilbakekreving.web.app.startupinfo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.health.HealthCheck;

import ch.qos.logback.classic.Level;
import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.SelftestResultat;
import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.Selftests;
import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks.ExtHealthCheck;
import no.nav.vedtak.log.util.MemoryAppender;

public class AppStartupInfoLoggerTest {
    private static MemoryAppender logSniffer = MemoryAppender.sniff(AppStartupInfoLogger.class);

    private AppStartupInfoLogger logger;

    @After
    public void afterEach() {
        logSniffer.reset();
    }

    @Before
    public void setup() {

        SelftestResultat samletResultat = new SelftestResultat();

        samletResultat.setApplication("minApp");
        samletResultat.setVersion("0.1");
        samletResultat.setTimestamp(LocalDateTime.now());
        samletResultat.setRevision("old");
        samletResultat.setBuildTime("long ago");

        HealthCheck.ResultBuilder builder = HealthCheck.Result.builder();
        builder.healthy();
        builder.withDetail(ExtHealthCheck.DETAIL_DESCRIPTION, "descr1");
        builder.withDetail(ExtHealthCheck.DETAIL_RESPONSE_TIME, "90ms");
        builder.withDetail(ExtHealthCheck.DETAIL_ENDPOINT, "http://ws.nav.no");
        samletResultat.leggTilResultatForIkkeKritiskTjeneste(builder.build());
        samletResultat.leggTilResultatForIkkeKritiskTjeneste(HealthCheck.Result.unhealthy("no2"));

        Selftests mockSelftests = mock(Selftests.class);
        when(mockSelftests.run()).thenReturn(samletResultat);

        logger = new AppStartupInfoLogger(mockSelftests);
    }

    @Test
    public void test() {
        logger.logAppStartupInfo();

        Assertions.assertThat(logSniffer.contains("OPPSTARTSINFO start", Level.INFO)).isTrue();
        Assertions.assertThat(logSniffer.contains("OPPSTARTSINFO slutt", Level.INFO)).isTrue();
        Assertions.assertThat(logSniffer.contains("", Level.WARN)).isFalse();
        Assertions.assertThat(logSniffer.contains("", Level.ERROR)).isFalse();
    }
}
