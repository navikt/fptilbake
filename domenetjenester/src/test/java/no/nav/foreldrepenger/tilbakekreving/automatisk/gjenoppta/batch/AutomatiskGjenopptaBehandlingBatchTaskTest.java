package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.batch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;

public class AutomatiskGjenopptaBehandlingBatchTaskTest {

    private AutomatiskGjenopptaBehandlingBatchTask batchTjeneste;

    private GjenopptaBehandlingTjeneste mockTjeneste;

    private Clock clock = Clock.fixed(Instant.parse("2020-05-04T12:00:00.00Z"), ZoneId.systemDefault());

    @Before
    public void setup() {
        mockTjeneste = mock(GjenopptaBehandlingTjeneste.class);
        batchTjeneste = new AutomatiskGjenopptaBehandlingBatchTask(mockTjeneste, clock);
    }

    @Test
    public void skal_ikke_kalle_gjenopptaBehandlinger_og_returnere_execution_id_i_helgen() {
        Clock clock = Clock.fixed(Instant.parse("2020-05-03T12:00:00.00Z"), ZoneId.systemDefault());
        batchTjeneste = new AutomatiskGjenopptaBehandlingBatchTask(mockTjeneste, clock);

        // Act
        batchTjeneste.doTask(null);

        // Verify
        verify(mockTjeneste, never()).automatiskGjenopptaBehandlinger();
    }

    @Test
    public void skal_kalle_gjenopptaBehandlinger_og_returnere_execution_id_ved_batch_launch() {
        // Act
        batchTjeneste.doTask(null);

        // Verify
        verify(mockTjeneste).automatiskGjenopptaBehandlinger();
    }

}
