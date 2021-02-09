package no.nav.foreldrepenger.tilbakekreving.kafka.poller;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.vedtak.log.util.MemoryAppender;

@Execution(ExecutionMode.SAME_THREAD)
@CdiDbAwareTest
public class KafkaPollerManagerTest {

    public MemoryAppender logSniffer;

    private KafkaPollerManager manager;

    @BeforeEach
    public void setUp(EntityManager entityManager) {
        logSniffer = MemoryAppender.sniff(KafkaPollerManager.class);
        @SuppressWarnings("unchecked")
        Instance<KafkaPoller> feedPollers = mock(Instance.class);
        @SuppressWarnings("unchecked")
        Iterator<KafkaPoller> iterator = mock(Iterator.class);

        lenient().when(feedPollers.get()).thenReturn(new TestKafkaPoller());
        lenient().when(feedPollers.iterator()).thenReturn(iterator);
        lenient().when(iterator.hasNext()).thenReturn(true, false);
        lenient().when(iterator.next()).thenReturn(new TestKafkaPoller());
        manager = new KafkaPollerManager(entityManager, feedPollers);
    }

    @AfterEach
    public void tearDown() {
        logSniffer.reset();
    }


    @Test
    public void skal_legge_til_poller() {
        manager.start();
        Assertions.assertThat(logSniffer.countEntries("Created thread for feed polling KafkaPollerManager-UnitTestPoller-poller")).isEqualTo(1);
        // MemoryAppender tar ikke debug ....
        //Assertions.assertThat(logSniffer.search("Lagt til ny poller til pollingtjeneste. poller=UnitTestPoller, delayBetweenPollingMillis=50", Level.DEBUG).size()).isEqualTo(1);
    }

    private class TestKafkaPoller implements KafkaPoller {

        @Override
        public String getName() {
            return "UnitTestPoller";
        }

        @Override
        public PostTransactionHandler poll() {
            return () -> {
            };
        }
    }
}
