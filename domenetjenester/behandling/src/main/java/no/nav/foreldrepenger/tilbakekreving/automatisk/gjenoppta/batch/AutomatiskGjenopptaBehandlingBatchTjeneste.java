package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.batch;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

@ApplicationScoped
public class AutomatiskGjenopptaBehandlingBatchTjeneste implements BatchTjeneste {

    static final String BATCHNAME = "BVL007";
    private static final String EXECUTION_ID_SEPARATOR = "-";
    private static final Logger logger = LoggerFactory.getLogger(AutomatiskGjenopptaBehandlingBatchTjeneste.class);

    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private Clock clock;

    AutomatiskGjenopptaBehandlingBatchTjeneste() {
        // CDI
    }

    @Inject
    public AutomatiskGjenopptaBehandlingBatchTjeneste(GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste) {
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.clock = Clock.systemDefaultZone();
    }

    // kun for test forbruk
    public AutomatiskGjenopptaBehandlingBatchTjeneste(GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                                                      Clock clock) {
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.clock = clock;
    }

    @Override
    public String launch(BatchArguments arguments) {
        String executionId = BATCHNAME + EXECUTION_ID_SEPARATOR;
        LocalDate iDag = LocalDate.now(clock);
        if (iDag.getDayOfWeek().equals(DayOfWeek.SATURDAY) || iDag.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            logger.info("I dag er helg, kan ikke kjøre batch-en {}", BATCHNAME);
            return executionId;
        }
        gjenopptaBehandlingTjeneste.automatiskGjenopptaBehandlinger();
        return executionId;
    }

    @Override
    public BatchStatus status(String executionId) {
        final String gruppe = executionId.substring(executionId.indexOf(EXECUTION_ID_SEPARATOR.charAt(0)) + 1);
        final List<TaskStatus> taskStatusListe = gjenopptaBehandlingTjeneste.hentStatusForGjenopptaBehandlingGruppe(gruppe);

        BatchStatus res;
        if (isCompleted(taskStatusListe)) {
            if (isContainingFailures(taskStatusListe)) {
                res = BatchStatus.WARNING;
            } else {
                res = BatchStatus.OK;
            }
        } else {
            res = BatchStatus.RUNNING;
        }
        return res;
    }

    @Override
    public String getBatchName() {
        return BATCHNAME;
    }

    private boolean isCompleted(List<TaskStatus> taskStatusListe) {
        return taskStatusListe.stream().noneMatch(it -> ProsessTaskStatus.KLAR.equals(it.getStatus()));
    }

    private boolean isContainingFailures(List<TaskStatus> taskStatusListe) {
        return taskStatusListe.stream().anyMatch(it -> ProsessTaskStatus.FEILET.equals(it.getStatus()));
    }
}
