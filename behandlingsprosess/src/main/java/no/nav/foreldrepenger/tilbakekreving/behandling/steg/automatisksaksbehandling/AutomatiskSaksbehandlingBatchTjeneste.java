package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling.AutomatiskSaksbehandlingRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class AutomatiskSaksbehandlingBatchTjeneste implements BatchTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(AutomatiskSaksbehandlingBatchTjeneste.class);
    private static final String BATCHNAVN = "BFPT-003";
    private static final String EXECUTION_ID_SEPARATOR = "-";

    private ProsessTaskRepository taskRepository;
    private AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository;
    private Clock clock;
    private Period grunnlagAlder;

    @Inject
    public AutomatiskSaksbehandlingBatchTjeneste(ProsessTaskRepository taskRepository,
                                                 AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository,
                                                 @KonfigVerdi(value = "grunnlag.alder") Period grunnlagAlder) {
        this.taskRepository = taskRepository;
        this.automatiskSaksbehandlingRepository = automatiskSaksbehandlingRepository;
        this.clock = Clock.systemDefaultZone();
        this.grunnlagAlder = grunnlagAlder;
    }

    // kun for testbruk
    protected AutomatiskSaksbehandlingBatchTjeneste(ProsessTaskRepository taskRepository,
                                                    AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository,
                                                    Clock clock,
                                                    @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.taskRepository = taskRepository;
        this.automatiskSaksbehandlingRepository = automatiskSaksbehandlingRepository;
        this.clock = clock;
        this.grunnlagAlder = grunnlagAlder;
    }

    @Override
    public String launch(BatchArguments arguments) {
        String batchRun = BATCHNAVN + EXECUTION_ID_SEPARATOR + UUID.randomUUID();
        LocalDate iDag = LocalDate.now(clock);
        if (iDag.getDayOfWeek().equals(DayOfWeek.SATURDAY) || iDag.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            logger.info("Kjører ikke batch {} i helgen. Iverksetting i saksbehandling avhenger av oppdragsystemet, som sannsynligvis har nedetid", BATCHNAVN);
        } else {
            LocalDate bestemtDato = iDag.minus(grunnlagAlder);
            logger.info("Henter behandlinger som er eldre enn {} i batch {}", bestemtDato, batchRun);
            List<Behandling> behandlinger = automatiskSaksbehandlingRepository.hentAlleBehandlingerSomErKlarForAutomatiskSaksbehandling(bestemtDato);
            behandlinger = behandlinger.stream().filter(behandling -> !behandling.isBehandlingPåVent()).collect(Collectors.toList());
            logger.info("Det finnes {} behandlinger som er klar for automatisk saksbehandling", behandlinger.size());
            behandlinger.forEach(behandling -> opprettAutomatiskSaksbehandlingProsessTask(batchRun, behandling));
        }
        return batchRun;
    }

    private void opprettAutomatiskSaksbehandlingProsessTask(String batchRun, Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskSaksbehandlingProsessTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskData.setSekvens("10");
        prosessTaskData.setGruppe(getGruppeNavn(batchRun));
        taskRepository.lagre(prosessTaskData);
    }

    @Override
    public BatchStatus status(String batchRun) {
        final String gruppe = getGruppeNavn(batchRun);
        final List<TaskStatus> taskStatusListe = taskRepository.finnStatusForTaskIGruppe(AutomatiskSaksbehandlingProsessTask.TASKTYPE, gruppe);

        BatchStatus res;
        if (isCompleted(taskStatusListe)) {
            if (isContainingFailures(taskStatusListe)) {
                res = BatchStatus.WARNING;
            } else {
                res = BatchStatus.OK;
            }
        } else {
            res = BatchStatus.RUNNING;
        }
        return res;
    }

    @Override
    public String getBatchName() {
        return BATCHNAVN;
    }

    private boolean isCompleted(List<TaskStatus> taskStatusListe) {
        return taskStatusListe.stream().noneMatch(it -> ProsessTaskStatus.KLAR.equals(it.getStatus()));
    }

    private boolean isContainingFailures(List<TaskStatus> taskStatusListe) {
        return taskStatusListe.stream().anyMatch(it -> ProsessTaskStatus.FEILET.equals(it.getStatus()));
    }

    private String getGruppeNavn(String batchRun) {
        return batchRun.substring(batchRun.indexOf(EXECUTION_ID_SEPARATOR.charAt(0)) + 1);
    }

}
