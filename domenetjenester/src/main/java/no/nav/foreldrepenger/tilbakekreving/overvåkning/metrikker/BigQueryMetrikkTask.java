package no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker.bigquery.tabell.ProsessTaskFeilTabell;
import no.nav.k9.felles.integrasjon.bigquery.klient.BigQueryKlient;
import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryRecord;
import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryTabellDefinisjon;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = BigQueryMetrikkTask.TASKTYPE, prioritet = 2, cronExpression = "0 */15 * * * *", maxFailedRuns = 20, firstDelay = 60)
public class BigQueryMetrikkTask implements ProsessTaskHandler {

    static final String TASKTYPE = "bigquery.metrikk.task";
    private static final String DATASET_NAME = "fptilbake_statistikk";

    private static final Logger LOG = LoggerFactory.getLogger(BigQueryMetrikkTask.class);

    private boolean lansert;
    private BigQueryKlient bigQueryKlient;
    private BigQueryStatistikkRepository statistikkRepository;

    BigQueryMetrikkTask() {
        // for CDI proxy
    }

    @Inject
    public BigQueryMetrikkTask(BigQueryKlient bigQueryKlient,
                               BigQueryStatistikkRepository statistikkRepository,
                               @KonfigVerdi(value = "toggle.enable.bigquery", defaultVerdi = "false") boolean lansert) {
        this.bigQueryKlient = bigQueryKlient;
        this.statistikkRepository = statistikkRepository;
        this.lansert = lansert;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        if (!lansert) {
            return;
        }

        long startTime = System.nanoTime();

        try {
            var metrikker = statistikkRepository.hentAlle();
            publiser(metrikker);
            LOG.info("Generert {} metrikker til BigQuery", metrikker.size());
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            throw e;
        } finally {
            var varighet = Duration.ofNanos(System.nanoTime() - startTime);
            if (Duration.ofSeconds(20).minus(varighet).isNegative()) {
                LOG.warn("Generering av BigQuery metrikker tok : " + varighet);
            } else {
                LOG.info("Generering av BigQuery metrikker tok : " + varighet);
            }
        }
    }

    private void publiser(List<? extends BigQueryRecord> metrikker) {
        if (metrikker.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        Map<BigQueryTabellDefinisjon, List<BigQueryRecord>> gruppertPerTabell = metrikker.stream()
            .collect(Collectors.groupingBy(BigQueryRecord::tabellDefinisjon));

        for (var entry : gruppertPerTabell.entrySet()) {
            BigQueryTabellDefinisjon tabellDefinisjon = entry.getKey();
            List<BigQueryRecord> records = entry.getValue();

            var rowMapper = tabellDefinisjon.getRowMapper(now);
            List<Map<String, ?>> rows = records.stream()
                .map(rowMapper)
                .toList();

            if (tabellDefinisjon.skalTømmeFørSkriv()) {
                bigQueryKlient.tømOgPubliserAtomisk(DATASET_NAME, tabellDefinisjon, rows);
            } else {
                var insertRows = rows.stream()
                    .map(row -> com.google.cloud.bigquery.InsertAllRequest.RowToInsert.of(row))
                    .toList();
                bigQueryKlient.publiser(DATASET_NAME, tabellDefinisjon, insertRows);
            }
        }
    }

    public void opprettAlleTabeller() {
        List<BigQueryTabellDefinisjon> tabeller = List.of(
            ProsessTaskFeilTabell.INSTANCE
        );

        tabeller.forEach(tabell -> bigQueryKlient.hentEllerOpprettTabell(DATASET_NAME, tabell));
    }
}
