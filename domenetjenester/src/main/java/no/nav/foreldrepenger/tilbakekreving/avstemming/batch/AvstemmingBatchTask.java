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

import no.nav.foreldrepenger.tilbakekreving.avstemming.AvstemmingTjeneste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
@ProsessTask(AvstemmingBatchTask.BATCHNAVN)
public class AvstemmingBatchTask implements ProsessTaskHandler {

    private Logger logger = LoggerFactory.getLogger(AvstemmingBatchTask.class);
    public static final String BATCHNAVN = "batch.avstemming";

    private static final DateTimeFormatter DATO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATO_TIDSPUNKT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final String FILNAVN_MAL = "%s-%s-%s-%s.csv";

    private String applikasjon;
    private AvstemmingTjeneste avstemmingTjeneste;
    private AvstemmingSftpBatchTjeneste sftpBatchTjeneste;

    private String miljø;

    AvstemmingBatchTask() {
        //for CDI proxy
    }

    @Inject
    public AvstemmingBatchTask(@KonfigVerdi(value = "app.name") String applikasjon,
                               AvstemmingTjeneste avstemmingTjeneste,
                               AvstemmingSftpBatchTjeneste sftpBatchTjeneste) {
        this.applikasjon = applikasjon;
        this.avstemmingTjeneste = avstemmingTjeneste;
        this.sftpBatchTjeneste = sftpBatchTjeneste;

        miljø = Environment.current().isProd() ? "p" : "q";
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String batchRun = BATCHNAVN + "-" + UUID.randomUUID();
        LocalDate dato = LocalDate.parse(prosessTaskData.getPropertyValue("dato"));
        logger.info("Kjører avstemming for {} i batch {}", dato, batchRun);

        Optional<String> resultat = avstemmingTjeneste.oppsummer(dato);

        if (resultat.isPresent()) {
            String forDato = dato.format(DATO_FORMATTER);
            String kjøreTidspunkt = LocalDateTime.now().format(DATO_TIDSPUNKT_FORMATTER);
            String filnavn = String.format(FILNAVN_MAL, applikasjon, miljø, forDato, kjøreTidspunkt);
            try {
                sftpBatchTjeneste.put(resultat.get(), filnavn);
                logger.info("Filen {} er overført til avstemming sftp", filnavn);
            } catch (JSchException | SftpException e ) {
                throw SftpFeilmelding.FEILFACTORY.overføringFeilet(filnavn, e).toException();
            }
        }
    }

    interface SftpFeilmelding extends DeklarerteFeil {
        SftpFeilmelding FEILFACTORY = FeilFactory.create(SftpFeilmelding.class);

        @IntegrasjonFeil(feilkode = "FPT-614386", feilmelding = "Overføring av fil [%s] til avstemming feilet.", logLevel = LogLevel.WARN)
        Feil overføringFeilet(String filnavn, Exception e);
    }
}
