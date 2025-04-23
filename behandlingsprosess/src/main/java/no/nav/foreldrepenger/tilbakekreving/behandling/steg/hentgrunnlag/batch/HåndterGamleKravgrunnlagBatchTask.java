package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;
import no.nav.foreldrepenger.tilbakekreving.felles.Helligdager;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "batch.håndter.gamle.kravgrunnlag", prioritet = 2, cronExpression = "0 15 7 ? * MON-FRI")
public class HåndterGamleKravgrunnlagBatchTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HåndterGamleKravgrunnlagBatchTask.class);

    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private ProsessTaskTjeneste taskTjeneste;
    private Period alderForGammeltGrunnlag;
    private Clock clock;

    HåndterGamleKravgrunnlagBatchTask() {
        // for CDI proxy
    }

    @Inject
    public HåndterGamleKravgrunnlagBatchTask(ØkonomiMottattXmlRepository mottattXmlRepository,
                                             ProsessTaskTjeneste taskTjeneste) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.taskTjeneste = taskTjeneste;
        this.clock = Clock.systemDefaultZone();
        this.alderForGammeltGrunnlag = Frister.KRAVGRUNNLAG_ALDER_GAMMELT;
    }

    // kun for test forbruk
    public HåndterGamleKravgrunnlagBatchTask(ØkonomiMottattXmlRepository mottattXmlRepository,
                                             ProsessTaskTjeneste taskTjeneste,
                                             Clock clock,
                                             Period alderForGammeltGrunnlag) {
        this.mottattXmlRepository = mottattXmlRepository;
        this.taskTjeneste = taskTjeneste;
        this.clock = clock;
        this.alderForGammeltGrunnlag = alderForGammeltGrunnlag;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var batchRun = this.getClass().getSimpleName() + "-" + UUID.randomUUID();
        var iDag = LocalDate.now(clock);

        // oppdragssystemtet er nede i helger og helligdager
        if (Helligdager.erHelligdagEllerHelg(iDag)) {
            LOG.info("I dag er helg/helligdag, kan ikke kjøre batch {}", batchRun);
        } else {
            var bestemtDato = iDag.minus(alderForGammeltGrunnlag);
            LOG.info("Håndterer kravgrunnlag som er eldre enn {} i batch {}", bestemtDato, batchRun);

            var alleGamleKravgrunnlag = hentGamleKravgrunnlag(bestemtDato);
            if (alleGamleKravgrunnlag.isEmpty()) {
                LOG.info("Det finnes ingen gammel kravgrunnlag før {}", bestemtDato);
            } else {
                LOG.info("Det finnes {} gamle kravgrunnlag før {}", alleGamleKravgrunnlag.size(), bestemtDato);
                lagProsessTask(batchRun, alleGamleKravgrunnlag);
            }
        }
    }

    private void lagProsessTask(String batchRun, List<Long> alleGamleKravgrunnlag) {
        var gruppe = "gammel-kravgrunnlag" + batchRun;
        for (var mottattXmlId : alleGamleKravgrunnlag) {
            var prosessTaskData = ProsessTaskData.forProsessTask(HåndterGamleKravgrunnlagTask.class);
            prosessTaskData.setProperty(TaskProperties.PROPERTY_MOTTATT_XML_ID, String.valueOf(mottattXmlId));
            prosessTaskData.setGruppe(gruppe);
            taskTjeneste.lagre(prosessTaskData);
        }
    }

    private List<Long> hentGamleKravgrunnlag(LocalDate bestemtDato) {
        LOG.info("Henter kravgrunnlag som er eldre enn {}", bestemtDato);
        return mottattXmlRepository.hentGamleUkobledeKravgrunnlagXmlIds(bestemtDato.atStartOfDay());
    }
}
