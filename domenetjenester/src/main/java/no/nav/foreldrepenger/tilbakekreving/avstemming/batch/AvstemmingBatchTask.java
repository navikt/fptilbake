package no.nav.foreldrepenger.tilbakekreving.avstemming.batch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.avstemming.AvstemFraResultatOgIverksettingStatusTjeneste;
import no.nav.foreldrepenger.tilbakekreving.avstemming.AvstemmingCsvFormatter;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "batch.avstemming", prioritet = 3, cronExpression = "0 55 6 ? * * ")
public class AvstemmingBatchTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AvstemmingBatchTask.class);

    private static final String APPNAME = ApplicationName.hvilkenTilbakeAppName();
    private static final DateTimeFormatter DATO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATO_TIDSPUNKT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final String FILNAVN_MAL = "%s-%s-%s-%s.csv";

    private AvstemFraResultatOgIverksettingStatusTjeneste avstemFraResultatOgIverksettingStatusTjeneste;
    private AvstemmingSftpBatchTjeneste sftpBatchTjeneste;

    private String miljø;

    AvstemmingBatchTask() {
        //for CDI proxy
    }

    @Inject
    public AvstemmingBatchTask(AvstemFraResultatOgIverksettingStatusTjeneste avstemFraResultatOgIverksettingStatusTjeneste,
                               AvstemmingSftpBatchTjeneste sftpBatchTjeneste) {
        this.avstemFraResultatOgIverksettingStatusTjeneste = avstemFraResultatOgIverksettingStatusTjeneste;
        this.sftpBatchTjeneste = sftpBatchTjeneste;

        miljø = Environment.current().isProd() ? "p" : "q";
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String batchRun = this.getClass().getSimpleName() + "-" + UUID.randomUUID();
        LocalDate dato = LocalDate.now().minusDays(1);
        LOG.info("Kjører avstemming for {} i batch {}", dato, batchRun);

        Optional<String> resultat = oppsummer(dato);

        if (resultat.isPresent()) {
            String forDato = dato.format(DATO_FORMATTER);
            String kjøreTidspunkt = LocalDateTime.now().format(DATO_TIDSPUNKT_FORMATTER);
            String filnavn = String.format(FILNAVN_MAL, APPNAME, miljø, forDato, kjøreTidspunkt);
            try {
                sftpBatchTjeneste.put(resultat.get(), filnavn);
                LOG.info("Filen {} er overført til avstemming sftp", filnavn);
            } catch (JSchException | SftpException e) {
                throw new IntegrasjonException("FPT-614386", String.format("Overføring av fil [%s] til avstemming feilet.", filnavn), e);
            }
        }
    }

    public Optional<String> oppsummer(LocalDate dato) {
        AvstemmingCsvFormatter avstemmingCsvFormatter = new AvstemmingCsvFormatter();

        avstemFraResultatOgIverksettingStatusTjeneste.leggTilOppsummering(dato, avstemmingCsvFormatter);

        LOG.info("Sender {} vedtak til avstemming for {}", avstemmingCsvFormatter.getAntallRader(), dato);

        if (avstemmingCsvFormatter.getAntallRader() == 0) {
            return Optional.empty();
        }
        return Optional.of(avstemmingCsvFormatter.getData());
    }
}
