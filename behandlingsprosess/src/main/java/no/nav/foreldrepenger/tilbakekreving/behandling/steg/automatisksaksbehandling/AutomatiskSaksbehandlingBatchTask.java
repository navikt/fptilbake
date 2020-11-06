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

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling.AutomatiskSaksbehandlingRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@ProsessTask(AutomatiskSaksbehandlingBatchTask.BATCHNAVN)
public class AutomatiskSaksbehandlingBatchTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(AutomatiskSaksbehandlingBatchTask.class);
    static final String BATCHNAVN = "batch.automatisk.saksbehandling";
    private static final String EXECUTION_ID_SEPARATOR = "-";

    private ProsessTaskRepository taskRepository;
    private AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository;
    private Clock clock;
    private Period grunnlagAlder;

    @Inject
    public AutomatiskSaksbehandlingBatchTask(ProsessTaskRepository taskRepository,
                                             AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository,
                                             @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.taskRepository = taskRepository;
        this.automatiskSaksbehandlingRepository = automatiskSaksbehandlingRepository;
        this.clock = Clock.systemDefaultZone();
        this.grunnlagAlder = grunnlagAlder;
    }

    // kun for testbruk
    protected AutomatiskSaksbehandlingBatchTask(ProsessTaskRepository taskRepository,
                                                AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository,
                                                Clock clock,
                                                @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.taskRepository = taskRepository;
        this.automatiskSaksbehandlingRepository = automatiskSaksbehandlingRepository;
        this.clock = clock;
        this.grunnlagAlder = grunnlagAlder;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
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
    }

    private void opprettAutomatiskSaksbehandlingProsessTask(String batchRun, Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskSaksbehandlingProsessTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskData.setSekvens("10");
        prosessTaskData.setGruppe(getGruppeNavn(batchRun));
        taskRepository.lagre(prosessTaskData);
    }

    private String getGruppeNavn(String batchRun) {
        return batchRun.substring(batchRun.indexOf(EXECUTION_ID_SEPARATOR.charAt(0)) + 1);
    }
}
