package no.nav.foreldrepenger.tilbakekreving.web.app.startupinfo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;

import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.SelftestResultat;
import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.Selftests;
import no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks.ExtHealthCheck;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;

@ApplicationScoped
class AppStartupInfoLogger {

    private static final Logger logger = LoggerFactory.getLogger(AppStartupInfoLogger.class);

    private Selftests selftests;

    private static final String OPPSTARTSINFO = "OPPSTARTSINFO";
    private static final String HILITE_SLUTT = "********";
    private static final String HILITE_START = HILITE_SLUTT;
    private static final String SELFTEST = "Selftest";
    private static final String APPLIKASJONENS_STATUS = "Applikasjonens status";
    private static final String START = "start:";
    private static final String SLUTT = "slutt.";

    AppStartupInfoLogger() {
        // for CDI proxy
    }

    @Inject
    AppStartupInfoLogger(Selftests selftests) {
        this.selftests = selftests;
    }

    void logAppStartupInfo() {
        log(HILITE_START + " " + OPPSTARTSINFO + " " + START + " " + HILITE_SLUTT);
        logSelftest();
        log(HILITE_START + " " + OPPSTARTSINFO + " " + SLUTT + " " + HILITE_SLUTT);
    }

    private void logSelftest() {
        log(SELFTEST + " " + START);

        // callId er påkrevd på utgående kall og må settes før selftest kjøres
        MDCOperations.putCallId();
        SelftestResultat samletResultat = selftests.run();
        MDCOperations.removeCallId();

        for (HealthCheck.Result result : samletResultat.getAlleResultater()) {
            log(result);
        }

        log(APPLIKASJONENS_STATUS + ": {}", samletResultat.getAggregateResult());

        log(SELFTEST + " " + SLUTT);
    }

    private void log(String msg, Object... args) {
        logger.info(msg, args); //NOSONAR
    }

    private void log(HealthCheck.Result result) {
        var details = result.getDetails();
        var detailsNotNull = details != null;
        logger.info(String.format("FPT-753409: Selftest %s: %s. Endpoint: %s. Responstid: %s. Feilmelding: %s.",
            getStatus(result.isHealthy()),
            detailsNotNull ? details.get(ExtHealthCheck.DETAIL_DESCRIPTION) : null,
            detailsNotNull ? details.get(ExtHealthCheck.DETAIL_ENDPOINT) : null,
            detailsNotNull ? details.get(ExtHealthCheck.DETAIL_RESPONSE_TIME) : null,
            result.getMessage()));
    }

    private String getStatus(boolean isHealthy) {
        return isHealthy ? "OK" : "ERROR";
    }
}
