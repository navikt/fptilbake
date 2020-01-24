package no.nav.foreldrepenger.tilbakekreving.avstemming.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.tilbakekreving.avstemming.AvstemmingTjeneste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class AvstemmingBatchTjeneste implements BatchTjeneste {

    private Logger logger = LoggerFactory.getLogger(AvstemmingBatchTjeneste.class);
    private static final String BATCHNAVN = "BFPT-001";

    private static final DateTimeFormatter DATO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATO_TIDSPUNKT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final String FILNAVN_MAL = "fptilbake-%s-%s-%s.csv";

    private AvstemmingTjeneste avstemmingTjeneste;
    private AvstemmingSftpBatchTjeneste sftpBatchTjeneste;

    private String miljø;

    AvstemmingBatchTjeneste() {
        //for CDI proxy
    }

    @Inject
    public AvstemmingBatchTjeneste(AvstemmingTjeneste avstemmingTjeneste,
                                   AvstemmingSftpBatchTjeneste sftpBatchTjeneste) {
        this.avstemmingTjeneste = avstemmingTjeneste;
        this.sftpBatchTjeneste = sftpBatchTjeneste;

        Optional<String> envName = EnvironmentProperty.getEnvironmentName();
        miljø = envName.orElseThrow();
    }

    @Override
    public String launch(BatchArguments arguments) {
        String batchRun = BATCHNAVN + "-" + UUID.randomUUID();
        AvstemmingBatchArgumenter argumenter = (AvstemmingBatchArgumenter) arguments;
        LocalDate dato = argumenter.getDato();
        logger.info("Kjører avstemming for {} i batch {}", dato, batchRun);

        Optional<String> resultat = avstemmingTjeneste.oppsummer(dato);

        if (resultat.isPresent()) {
            String forDato = dato.format(DATO_FORMATTER);
            String kjøreTidspunkt = FPDateUtil.nå().format(DATO_TIDSPUNKT_FORMATTER);
            String filnavn = String.format(FILNAVN_MAL, miljø, forDato, kjøreTidspunkt);
            try {
                sftpBatchTjeneste.put(resultat.get(), filnavn);
                logger.info("Filen {} er overført til avstemming sftp", filnavn);
            } catch (JSchException | SftpException e ) {
                throw SftpFeilmelding.FEILFACTORY.overføringFeilet(filnavn, e).toException();
            }
        }

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

    interface SftpFeilmelding extends DeklarerteFeil {
        SftpFeilmelding FEILFACTORY = FeilFactory.create(SftpFeilmelding.class);

        @IntegrasjonFeil(feilkode = "FPT-614386", feilmelding = "Overføring av fil [%s] til avstemming feilet.", logLevel = LogLevel.WARN)
        Feil overføringFeilet(String filnavn, Exception e);
    }
}
