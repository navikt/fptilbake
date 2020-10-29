package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class HåndterGamleKravgrunnlagBatchTjeneste implements BatchTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(HåndterGamleKravgrunnlagBatchTjeneste.class);
    private static final String BATCHNAVN = "BFPT-002";

    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private ProsessTaskRepository taskRepository;
    private Clock clock;
    private Period grunnlagAlder;

    HåndterGamleKravgrunnlagBatchTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagBatchTjeneste(ØkonomiMottattXmlRepository mottattXmlRepository,
                                                 ProsessTaskRepository taskRepository,
                                                 @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.taskRepository = taskRepository;
        this.clock = Clock.systemDefaultZone();
        this.grunnlagAlder = grunnlagAlder;
    }

    // kun for test forbruk
    public HåndterGamleKravgrunnlagBatchTjeneste(ØkonomiMottattXmlRepository mottattXmlRepository,
                                                 ProsessTaskRepository taskRepository,
                                                 Clock clock,
                                                 @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.taskRepository = taskRepository;
        this.clock = clock;
        this.grunnlagAlder = grunnlagAlder;
    }

    @Override
    public String launch(BatchArguments arguments) {
        String batchRun = BATCHNAVN + "-" + UUID.randomUUID();
        LocalDate iDag = LocalDate.now(clock);
        if (iDag.getDayOfWeek().equals(DayOfWeek.SATURDAY) || iDag.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            logger.info("I dag er helg, kan ikke kjøre batchen {}", BATCHNAVN);
        } else {
            LocalDate bestemtDato = iDag.minus(grunnlagAlder);
            logger.info("Håndterer kravgrunnlag som er eldre enn {} i batch {}", bestemtDato, batchRun);

            List<Long> alleGamleKravgrunnlag = hentGamlekravgrunnlag(bestemtDato);
            if (alleGamleKravgrunnlag.isEmpty()) {
                logger.info("Det finnes ingen gammel kravgrunnlag før {}", bestemtDato);
            } else {
                logger.info("Det finnes {} gamle kravgrunnlag før {}", alleGamleKravgrunnlag.size(), bestemtDato);
                lagProsessTask(batchRun, alleGamleKravgrunnlag);
            }
        }
        return batchRun;
    }

    private void lagProsessTask(String batchRun, List<Long> alleGamleKravgrunnlag) {
        String gruppe = "gammel-kravgrunnlag" + batchRun;
        for (Long mottattXmlId : alleGamleKravgrunnlag) {
            ProsessTaskData prosessTaskData = new ProsessTaskData(HåndterGamleKravgrunnlagTask.TASKTYPE);
            prosessTaskData.setProperty("mottattXmlId",String.valueOf(mottattXmlId));
            prosessTaskData.setCallIdFraEksisterende();
            prosessTaskData.setGruppe(gruppe);
            taskRepository.lagre(prosessTaskData);
        }
    }

    private List<Long> hentGamlekravgrunnlag(LocalDate bestemtDato) {
        logger.info("Henter kravgrunnlag som er eldre enn {}", bestemtDato);
        return mottattXmlRepository.hentGamleUkobledeKravgrunnlagXmlIds(bestemtDato.atStartOfDay());
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

}
