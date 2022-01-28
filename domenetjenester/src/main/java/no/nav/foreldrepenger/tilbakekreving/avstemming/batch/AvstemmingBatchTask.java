package no.nav.foreldrepenger.tilbakekreving.avstemming.batch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.avstemming.AvstemmingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "batch.avstemming", cronExpression = "0 55 6 ? * * ")
public class AvstemmingBatchTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(AvstemmingBatchTask.class);

    private static final String APPNAME = ApplicationName.hvilkenTilbakeAppName();
    private static final DateTimeFormatter DATO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATO_TIDSPUNKT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final String FILNAVN_MAL = "%s-%s-%s-%s.csv";

    private AvstemmingTjeneste avstemmingTjeneste;
    private AvstemmingSftpBatchTjeneste sftpBatchTjeneste;

    private String miljø;

    AvstemmingBatchTask() {
        //for CDI proxy
    }

    @Inject
    public AvstemmingBatchTask(AvstemmingTjeneste avstemmingTjeneste,
                               AvstemmingSftpBatchTjeneste sftpBatchTjeneste) {
        this.avstemmingTjeneste = avstemmingTjeneste;
        this.sftpBatchTjeneste = sftpBatchTjeneste;

        miljø = Environment.current().isProd() ? "p" : "q";
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String batchRun = this.getClass().getSimpleName() + "-" + UUID.randomUUID();
        LocalDate dato = LocalDate.now().minusDays(1);
        logger.info("Kjører avstemming for {} i batch {}", dato, batchRun);

        Optional<String> resultat = avstemmingTjeneste.oppsummer(dato);

        if (resultat.isPresent()) {
            String forDato = dato.format(DATO_FORMATTER);
            String kjøreTidspunkt = LocalDateTime.now().format(DATO_TIDSPUNKT_FORMATTER);
            String filnavn = String.format(FILNAVN_MAL, APPNAME, miljø, forDato, kjøreTidspunkt);
            try {
                sftpBatchTjeneste.put(resultat.get(), filnavn);
                logger.info("Filen {} er overført til avstemming sftp", filnavn);
            } catch (JSchException | SftpException e) {
                throw new IntegrasjonException("FPT-614386", String.format("Overføring av fil [%s] til avstemming feilet.", filnavn), e);
            }
        }
    }
}
