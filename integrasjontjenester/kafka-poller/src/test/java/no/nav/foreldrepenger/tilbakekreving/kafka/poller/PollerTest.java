package no.nav.foreldrepenger.tilbakekreving.kafka.poller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.qos.logback.classic.Level;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class PollerTest {

    @Rule
    public LogSniffer logSniffer = new LogSniffer(Level.ALL);

    private Poller poller;
    private KafkaPoller kafkaPoller;

    @Before
    public void setUp() {
        kafkaPoller = mock(KafkaPoller.class);
        when(kafkaPoller.getName()).thenReturn("UnitTestPoller");
        poller = new Poller();
    }

    @After
    public void tearDown() {
        logSniffer.clearLog();
    }

    @Test
    public void skal_logge_exception_ved_feil_ved_polling() {
        Poller pollerSomFårNPE = new Poller(null, null);

        pollerSomFårNPE.run();
        logSniffer.assertHasWarnMessage("FP-852160:Kunne ikke polle kafka hendelser, venter til neste runde(runde=1)");
    }

    @Test
    public void skal_behandle_ukjent_feil() {
        doThrow(new RuntimeException()).when(kafkaPoller).poll();
        poller.run();

        logSniffer.assertHasWarnMessage("FP-852160:Kunne ikke polle kafka hendelser, venter til neste runde(runde=1)");
    }
}
