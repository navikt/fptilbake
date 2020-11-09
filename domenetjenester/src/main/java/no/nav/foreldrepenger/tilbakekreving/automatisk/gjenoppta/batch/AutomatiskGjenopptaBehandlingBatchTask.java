package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.batch;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(AutomatiskGjenopptaBehandlingBatchTask.BATCHNAVN)
public class AutomatiskGjenopptaBehandlingBatchTask implements ProsessTaskHandler {

    public static final String BATCHNAVN = "batch.ta.behandling.av.vent";
    private static final Logger logger = LoggerFactory.getLogger(AutomatiskGjenopptaBehandlingBatchTask.class);

    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private Clock clock;

    AutomatiskGjenopptaBehandlingBatchTask() {
        // CDI
    }

    @Inject
    public AutomatiskGjenopptaBehandlingBatchTask(GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste) {
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.clock = Clock.systemDefaultZone();
    }

    // kun for test forbruk
    public AutomatiskGjenopptaBehandlingBatchTask(GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                                                  Clock clock) {
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.clock = clock;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        LocalDate iDag = LocalDate.now(clock);
        if (iDag.getDayOfWeek().equals(DayOfWeek.SATURDAY) || iDag.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            logger.info("I dag er helg, kan ikke kjøre batch-en {}", BATCHNAVN);
        }else {
            gjenopptaBehandlingTjeneste.automatiskGjenopptaBehandlinger();
        }
    }
}
