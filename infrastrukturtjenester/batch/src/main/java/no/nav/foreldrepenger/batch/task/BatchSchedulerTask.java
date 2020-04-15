package no.nav.foreldrepenger.batch.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.batch.BatchSupportTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * Enkel scheduler for dagens situasjon der man kjører batcher mandag-fredag og det er noe variasjon i parametere.
 * <p>
 * Kan evt endres slik at BatchSchedulerTask kjører tidlig på døgnet og oppretter dagens batches (hvis ikke tidspunkt passert)
 * <p>
 * Skal man utvide med ukentlige, måndedlige batcher etc bør man se på cron-aktige uttrykk for spesifikasjon av kjøring.
 * FC har implementert et rammeverk på github
 */
@ApplicationScoped
@ProsessTask(BatchSchedulerTask.TASKTYPE)
public class BatchSchedulerTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "batch.scheduler";
    public static final String BATCH_AVSTEMMING = "BFPT-001";
    public static final String BATCH_GAMLE_KRAVGRUNNLAG_UTEN_BEHANDLING = "BFPT-002";
    public static final String BATCH_TA_AV_VENT = "BVL007";

    private BatchSupportTjeneste batchSupportTjeneste;

    private final List<Supplier<BatchConfig>> batchOppsettFelles = Arrays.asList(
        () -> new BatchConfig(6, 55, BATCH_AVSTEMMING, "dato=" + LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE)),
        () -> new BatchConfig(7, 0, BATCH_TA_AV_VENT, null),
        () -> new BatchConfig(8, 0, BATCH_GAMLE_KRAVGRUNNLAG_UTEN_BEHANDLING, null)
    );

    private LocalDate dagensDato;

    BatchSchedulerTask() {
        // for CDI proxy
    }

    @Inject
    public BatchSchedulerTask(BatchSupportTjeneste batchSupportTjeneste) {
        this.batchSupportTjeneste = batchSupportTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        dagensDato = LocalDate.now();

        // Lagre neste instans av daglig scheduler straks over midnatt
        ProsessTaskData batchScheduler = new ProsessTaskData(BatchSchedulerTask.TASKTYPE);
        LocalDateTime nesteScheduler = dagensDato.plusDays(1).atStartOfDay().plusHours(1).plusMinutes(1);
        batchScheduler.setNesteKjøringEtter(nesteScheduler);
        ProsessTaskGruppe gruppeScheduler = new ProsessTaskGruppe(batchScheduler);
        batchSupportTjeneste.opprettScheduledTasks(gruppeScheduler);

        List<BatchConfig> batchOppsett = batchOppsettFelles.stream()
            .map(Supplier::get)
            .collect(Collectors.toList());

        List<ProsessTaskData> batchtasks = batchOppsett.stream()
            .map(this::mapBatchConfigTilBatchRunnerTask)
            .collect(Collectors.toList());
        ProsessTaskGruppe gruppeRunner = new ProsessTaskGruppe();
        gruppeRunner.addNesteParallell(batchtasks);

        batchSupportTjeneste.opprettScheduledTasks(gruppeRunner);
    }

    private ProsessTaskData mapBatchConfigTilBatchRunnerTask(BatchConfig config) {
        ProsessTaskData batchRunnerTask = new ProsessTaskData(BatchRunnerTask.TASKTYPE);
        batchRunnerTask.setProperty(BatchRunnerTask.BATCH_NAME, config.getName());
        if (config.getParams() != null) {
            batchRunnerTask.setProperty(BatchRunnerTask.BATCH_PARAMS, config.getParams());
        }
        batchRunnerTask.setProperty(BatchRunnerTask.BATCH_RUN_DATE, dagensDato.toString());
        batchRunnerTask.setNesteKjøringEtter(LocalDateTime.of(dagensDato, config.getKjøreTidspunkt()));
        return batchRunnerTask;
    }
}
