package no.nav.foreldrepenger.tilbakekreving.web.app.selftest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.felles.KafkaIntegration;
import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks.DatabaseHealthCheck;
import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks.KravgrunnlagQueueHealthCheck;

@ApplicationScoped
public class Selftests {

    private DatabaseHealthCheck databaseHealthCheck;
    private KravgrunnlagQueueHealthCheck kravgrunnlagQueueHealthCheck;
    private Map<KafkaIntegration, AtomicBoolean> serviceMap = new HashMap<>();

    private boolean isReady;
    private LocalDateTime sistOppdatertTid = LocalDateTime.now().minusDays(1);

    @Inject
    public Selftests(@Any Instance<KafkaIntegration> serviceHandlers,
                     DatabaseHealthCheck databaseHealthCheck,
                     KravgrunnlagQueueHealthCheck kravgrunnlagQueueHealthCheck) {
        this.databaseHealthCheck = databaseHealthCheck;
        this.kravgrunnlagQueueHealthCheck = kravgrunnlagQueueHealthCheck;
        serviceHandlers.forEach(handler -> serviceMap.put(handler, new AtomicBoolean()));
    }

    Selftests() {
        // for CDI proxy
    }

    public Selftests.Resultat run() {
        oppdaterSelftestResultatHvisNødvendig();
        return new Selftests.Resultat(isReady, databaseHealthCheck.getDescription(), databaseHealthCheck.getEndpoint());
    }

    public boolean isKafkaAlive() {
        return serviceMap.entrySet()
            .stream()
            .filter(it -> it.getKey() != null)
            .allMatch(it -> it.getKey().isAlive());
    }

    public boolean isReady() {
        // Bruk denne for NAIS-respons og skill omfanget her.
        return run().isReady();
    }

    private synchronized void oppdaterSelftestResultatHvisNødvendig() {
        if (sistOppdatertTid.isBefore(LocalDateTime.now().minusSeconds(30))) {
            isReady = databaseHealthCheck.isOK() && kravgrunnlagQueueHealthCheck.isOk();
            sistOppdatertTid = LocalDateTime.now();
        }
    }

    public record Resultat(boolean isReady, String description, String endpoint) {
    }

}
