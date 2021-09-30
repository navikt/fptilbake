package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.felles.Helligdager;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "batch.håndter.gamle.kravgrunnlag", cronExpression = "0 15 7 ? * MON-FRI")
public class HåndterGamleKravgrunnlagBatchTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(HåndterGamleKravgrunnlagBatchTask.class);

    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private ProsessTaskTjeneste taskTjeneste;
    private Clock clock;
    private Period grunnlagAlder;

    HåndterGamleKravgrunnlagBatchTask() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagBatchTask(ØkonomiMottattXmlRepository mottattXmlRepository,
                                             ProsessTaskTjeneste taskTjeneste,
                                             @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.taskTjeneste = taskTjeneste;
        this.clock = Clock.systemDefaultZone();
        this.grunnlagAlder = grunnlagAlder;
    }

    // kun for test forbruk
    public HåndterGamleKravgrunnlagBatchTask(ØkonomiMottattXmlRepository mottattXmlRepository,
                                             ProsessTaskTjeneste taskTjeneste,
                                             Clock clock,
                                             @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.taskTjeneste = taskTjeneste;
        this.clock = clock;
        this.grunnlagAlder = grunnlagAlder;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String batchRun = this.getClass().getSimpleName() + "-" + UUID.randomUUID();
        LocalDate iDag = LocalDate.now(clock);

        // oppdragssystemtet er nede i helger og helligdager
        if (Helligdager.erHelligdagEllerHelg(iDag)) {
            logger.info("I dag er helg/helligdag, kan ikke kjøre batchen {}", batchRun);
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
            ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(HåndterGamleKravgrunnlagTask.class);
            prosessTaskData.setProperty("mottattXmlId", String.valueOf(mottattXmlId));
            prosessTaskData.setCallIdFraEksisterende();
            prosessTaskData.setGruppe(gruppe);
            taskTjeneste.lagre(prosessTaskData);
        }
    }

    private List<Long> hentGamlekravgrunnlag(LocalDate bestemtDato) {
        logger.info("Henter kravgrunnlag som er eldre enn {}", bestemtDato);
        return mottattXmlRepository.hentGamleUkobledeKravgrunnlagXmlIds(bestemtDato.atStartOfDay());
    }
}
