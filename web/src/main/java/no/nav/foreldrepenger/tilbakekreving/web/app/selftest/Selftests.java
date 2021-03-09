package no.nav.foreldrepenger.tilbakekreving.web.app.selftest;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks.DatabaseHealthCheck;
import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks.KravgrunnlagQueueHealthCheck;

@ApplicationScoped
public class Selftests {

    private DatabaseHealthCheck databaseHealthCheck;
    private KravgrunnlagQueueHealthCheck kravgrunnlagQueueHealthCheck;

    private boolean isReady;
    private LocalDateTime sistOppdatertTid = LocalDateTime.now().minusDays(1);

    @Inject
    public Selftests(DatabaseHealthCheck databaseHealthCheck,
                     KravgrunnlagQueueHealthCheck kravgrunnlagQueueHealthCheck) {
        this.databaseHealthCheck = databaseHealthCheck;
        this.kravgrunnlagQueueHealthCheck = kravgrunnlagQueueHealthCheck;
    }

    Selftests() {
        // for CDI proxy
    }

    public Selftests.Resultat run() {
        oppdaterSelftestResultatHvisNødvendig();
        return new Selftests.Resultat(isReady, databaseHealthCheck.getDescription(), databaseHealthCheck.getEndpoint());
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

    public static class Resultat {
        private final boolean isReady;
        private final String description;
        private final String endpoint;

        public Resultat(boolean isReady, String description, String endpoint) {
            this.isReady = isReady;
            this.description = description;
            this.endpoint = endpoint;
        }

        public boolean isReady() {
            return isReady;
        }

        public String getDescription() {
            return description;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }

}
