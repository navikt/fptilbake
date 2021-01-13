package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import no.nav.foreldrepenger.tilbakekreving.felles.Helligdager;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@ProsessTask(HåndterGamleKravgrunnlagBatchTask.BATCHNAVN)
public class HåndterGamleKravgrunnlagBatchTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(HåndterGamleKravgrunnlagBatchTask.class);
    public static final String BATCHNAVN = "batch.håndter.gamle.kravgrunnlag";

    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private ProsessTaskRepository taskRepository;
    private Clock clock;
    private Period grunnlagAlder;

    HåndterGamleKravgrunnlagBatchTask() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagBatchTask(ØkonomiMottattXmlRepository mottattXmlRepository,
                                             ProsessTaskRepository taskRepository,
                                             @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.taskRepository = taskRepository;
        this.clock = Clock.systemDefaultZone();
        this.grunnlagAlder = grunnlagAlder;
    }

    // kun for test forbruk
    public HåndterGamleKravgrunnlagBatchTask(ØkonomiMottattXmlRepository mottattXmlRepository,
                                             ProsessTaskRepository taskRepository,
                                             Clock clock,
                                             @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.taskRepository = taskRepository;
        this.clock = clock;
        this.grunnlagAlder = grunnlagAlder;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String batchRun = BATCHNAVN + "-" + UUID.randomUUID();
        LocalDate iDag = LocalDate.now(clock);

        // oppdragssystemtet er nede i helger og helligdager
        if (Helligdager.erHelligdagEllerHelg(iDag)) {
            logger.info("I dag er helg/helligdag, kan ikke kjøre batchen {}", BATCHNAVN);
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
    }

    private void lagProsessTask(String batchRun, List<Long> alleGamleKravgrunnlag) {
        String gruppe = "gammel-kravgrunnlag" + batchRun;
        for (Long mottattXmlId : alleGamleKravgrunnlag) {
            ProsessTaskData prosessTaskData = new ProsessTaskData(HåndterGamleKravgrunnlagTask.TASKTYPE);
            prosessTaskData.setProperty("mottattXmlId", String.valueOf(mottattXmlId));
            prosessTaskData.setCallIdFraEksisterende();
            prosessTaskData.setGruppe(gruppe);
            taskRepository.lagre(prosessTaskData);
        }
    }

    private List<Long> hentGamlekravgrunnlag(LocalDate bestemtDato) {
        logger.info("Henter kravgrunnlag som er eldre enn {}", bestemtDato);
        return mottattXmlRepository.hentGamleUkobledeKravgrunnlagXmlIds(bestemtDato.atStartOfDay());
    }
}
