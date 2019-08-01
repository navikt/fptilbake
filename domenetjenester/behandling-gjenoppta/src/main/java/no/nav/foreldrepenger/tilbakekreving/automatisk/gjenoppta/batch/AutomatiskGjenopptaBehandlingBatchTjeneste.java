package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.batch;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    public AutomatiskGjenopptaBehandlingBatchTjeneste() {
        // CDI
    }

    @Inject
    public AutomatiskGjenopptaBehandlingBatchTjeneste(GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste) {
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
    }

    @Override
    public String launch(BatchArguments arguments) {
        gjenopptaBehandlingTjeneste.automatiskGjenopptaBehandlinger();
        String executionId = BATCHNAME + EXECUTION_ID_SEPARATOR;
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
