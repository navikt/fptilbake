package no.nav.foreldrepenger.tilbakekreving.avstemming.batch;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.tilbakekreving.avstemming.AvstemmingTjeneste;

@ApplicationScoped
public class AvstemmingBatchTjeneste implements BatchTjeneste {

    private Logger logger = LoggerFactory.getLogger(AvstemmingBatchTjeneste.class);
    private static final String BATCHNAVN = "BFPT-001";

    private AvstemmingTjeneste avstemmingTjeneste;

    AvstemmingBatchTjeneste() {
        //for CDI proxy
    }

    @Inject
    public AvstemmingBatchTjeneste(AvstemmingTjeneste avstemmingTjeneste) {
        this.avstemmingTjeneste = avstemmingTjeneste;
    }

    @Override
    public String launch(BatchArguments arguments) {
        String batchRun = BATCHNAVN + "-" + UUID.randomUUID();
        AvstemmingBatchArgumenter argumenter = (AvstemmingBatchArgumenter) arguments;
        LocalDate dato = argumenter.getDato();
        logger.info("Kjører avstemming for {} i batch {}", dato, batchRun);

        Optional<String> resultat = avstemmingTjeneste.oppsummer(dato);

        //FIXME send med sftp til avstemmingsserver

        return batchRun;
    }

    @Override
    public BatchStatus status(String batchInstanceNumber) {
        // Antar her at alt har gått bra siden denne er en synkron jobb.
        return BatchStatus.OK;
    }

    @Override
    public String getBatchName() {
        return BATCHNAVN;
    }

    @Override
    public AvstemmingBatchArgumenter createArguments(Map<String, String> jobArguments) {
        return new AvstemmingBatchArgumenter(jobArguments);
    }
}
