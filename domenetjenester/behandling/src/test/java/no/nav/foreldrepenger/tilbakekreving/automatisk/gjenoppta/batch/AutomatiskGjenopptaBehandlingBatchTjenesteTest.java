package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.batch;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

public class AutomatiskGjenopptaBehandlingBatchTjenesteTest {

    private AutomatiskGjenopptaBehandlingBatchTjeneste batchTjeneste;

    private GjenopptaBehandlingTjeneste mockTjeneste;

    public static final String BATCHNAME = AutomatiskGjenopptaBehandlingBatchTjeneste.BATCHNAME;
    public static final String GRUPPE = "1023";
    public static final String EXECUTION_ID = BATCHNAME + "-" + GRUPPE;

    private static final TaskStatus FERDIG_1 = new TaskStatus(ProsessTaskStatus.FERDIG, new BigDecimal(1));
    private static final TaskStatus FERDIG_2 = new TaskStatus(ProsessTaskStatus.FERDIG, new BigDecimal(1));
    private static final TaskStatus FEILET_1 = new TaskStatus(ProsessTaskStatus.FEILET, new BigDecimal(1));
    private static final TaskStatus KLAR_1 = new TaskStatus(ProsessTaskStatus.KLAR, new BigDecimal(1));
    private Clock clock = Clock.fixed(Instant.parse("2020-05-04T12:00:00.00Z"), ZoneId.systemDefault());

    @Before
    public void setup() {
        mockTjeneste = mock(GjenopptaBehandlingTjeneste.class);
        batchTjeneste = new AutomatiskGjenopptaBehandlingBatchTjeneste(mockTjeneste, clock);
    }

    @Test
    public void skal_ikke_kalle_gjenopptaBehandlinger_og_returnere_execution_id_i_helgen() {
        Clock clock = Clock.fixed(Instant.parse("2020-05-03T12:00:00.00Z"), ZoneId.systemDefault());
        batchTjeneste = new AutomatiskGjenopptaBehandlingBatchTjeneste(mockTjeneste, clock);

        // Act
        batchTjeneste.launch(null);

        // Verify
        verify(mockTjeneste, never()).automatiskGjenopptaBehandlinger();
    }

    @Test
    public void skal_kalle_gjenopptaBehandlinger_og_returnere_execution_id_ved_batch_launch() {
        // Act
        batchTjeneste.launch(null);

        // Verify
        verify(mockTjeneste).automatiskGjenopptaBehandlinger();
    }

    @Test
    public void skal_gi_status_ok_når_alle_tasks_ferdig_uten_feil() {
        // Arrange
        when(mockTjeneste.hentStatusForGjenopptaBehandlingGruppe(GRUPPE)).thenReturn(asList(FERDIG_1, FERDIG_2));

        // Act
        BatchStatus batchStatus = batchTjeneste.status(EXECUTION_ID);

        // Assert
        assertThat(batchStatus).isEqualTo(BatchStatus.OK);
    }

    @Test
    public void skal_gi_status_ok_når_ingen_tasks_funnet() {
        // Arrange
        when(mockTjeneste.hentStatusForGjenopptaBehandlingGruppe(GRUPPE)).thenReturn(Collections.emptyList());

        // Act
        BatchStatus batchStatus = batchTjeneste.status(EXECUTION_ID);

        // Assert
        assertThat(batchStatus).isEqualTo(BatchStatus.OK);
    }

    @Test
    public void skal_gi_status_warning_når_minst_en_task_feilet() {
        // Arrange
        when(mockTjeneste.hentStatusForGjenopptaBehandlingGruppe(GRUPPE)).thenReturn(asList(FERDIG_1, FEILET_1));

        // Act
        BatchStatus batchStatus = batchTjeneste.status(EXECUTION_ID);

        // Assert
        assertThat(batchStatus).isEqualTo(BatchStatus.WARNING);
    }

    @Test
    public void skal_gi_status_running_når_minst_en_task_ikke_er_startet() {
        // Arrange
        when(mockTjeneste.hentStatusForGjenopptaBehandlingGruppe(GRUPPE)).thenReturn(asList(FERDIG_1, FEILET_1, KLAR_1));

        // Act
        BatchStatus batchStatus = batchTjeneste.status(EXECUTION_ID);

        // Assert
        assertThat(batchStatus).isEqualTo(BatchStatus.RUNNING);
    }

}
