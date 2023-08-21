package no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.sensu.SensuEvent;
import no.nav.foreldrepenger.tilbakekreving.sensu.SensuKlient;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = SensuMetrikkTask.TASKTYPE, cronExpression = "0 */15 * * * *", maxFailedRuns = 20, firstDelay = 60)
public class SensuMetrikkTask implements ProsessTaskHandler {

    private static final int CHUNK_EVENT_SIZE = 1000;

    static final String TASKTYPE = "sensu.metrikk.task";

    private static final Logger log = LoggerFactory.getLogger(SensuMetrikkTask.class);
    private boolean lansert;

    private SensuKlient sensuKlient;

    private StatistikkRepository statistikkRepository;

    SensuMetrikkTask() {
        // for proxyd
    }

    @Inject
    public SensuMetrikkTask(SensuKlient sensuKlient,
                            StatistikkRepository statistikkRepository,
                            @KonfigVerdi(value = "toggle.enable.sensu", defaultVerdi = "false") boolean lansert) {
        this.sensuKlient = sensuKlient;
        this.statistikkRepository = statistikkRepository;
        this.lansert = lansert;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        if (!lansert) {
            return;
        }
        log.info("Publisering vha sensu er skrudd på");

        long startTime = System.nanoTime();

        try {
            var metrikker = statistikkRepository.hentAlle();
            logMetrics(metrikker);
            log.info("Generert {} metrikker til sensu", metrikker.size());
        } catch (Exception e){
            log.warn(e.getMessage(), e);
            throw e;
        } finally {
            var varighet = Duration.ofNanos(System.nanoTime() - startTime);
            if (Duration.ofSeconds(20).minus(varighet).isNegative()) {
                // bruker for lang tid på logging av metrikker.
                log.warn("Generering av sensu metrikker tok : " + varighet);
            } else {
                log.info("Generering av sensu metrikker tok : " + varighet);
            }
        }

    }

    private void logMetrics(List<SensuEvent> events) {
        var counter = new AtomicInteger();
        var chunkSize = CHUNK_EVENT_SIZE;
        Map<Integer, List<SensuEvent>> chunked = events.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize));
        chunked.entrySet().forEach(e -> sensuKlient.logMetrics(e.getValue()));
    }
}
