package no.nav.foreldrepenger.tilbakekreving.kafka.poller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.qos.logback.classic.Level;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.vedtak.log.util.MemoryAppender;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class PollerTest {

    public MemoryAppender logSniffer;

    private Poller poller;
    private KafkaPoller kafkaPoller;

    @BeforeEach
    public void setUp() {
        logSniffer = MemoryAppender.sniff(Poller.class);
        kafkaPoller = mock(KafkaPoller.class);
        lenient().when(kafkaPoller.getName()).thenReturn("UnitTestPoller");
        poller = new Poller();
    }

    @AfterEach
    public void tearDown() {
        logSniffer.reset();
    }

    @Test
    public void skal_logge_exception_ved_feil_ved_polling() {
        Poller pollerSomFårNPE = new Poller(null, null);

        pollerSomFårNPE.run();
        Assertions.assertThat(logSniffer.search("FP-852160:Kunne ikke polle kafka hendelser, venter til neste runde(runde=1)")).isNotEmpty();
    }

    @Test
    public void skal_behandle_ukjent_feil() {
        doThrow(new RuntimeException()).when(kafkaPoller).poll();
        poller.run();

        Assertions.assertThat(logSniffer.search("FP-852160:Kunne ikke polle kafka hendelser, venter til neste runde(runde=1)", Level.WARN)).isNotEmpty();
    }
}
