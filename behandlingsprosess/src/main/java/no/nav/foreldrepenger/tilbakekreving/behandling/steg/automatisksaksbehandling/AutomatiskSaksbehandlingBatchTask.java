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

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling.AutomatiskSaksbehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Helligdager;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "batch.automatisk.saksbehandling", cronExpression = "0 30 7 ? * MON-FRI")
public class AutomatiskSaksbehandlingBatchTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(AutomatiskSaksbehandlingBatchTask.class);
    private static final String EXECUTION_ID_SEPARATOR = "-";

    private ProsessTaskTjeneste taskTjeneste;
    private AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository;
    private Clock clock;
    private Period grunnlagAlder;

    @Inject
    public AutomatiskSaksbehandlingBatchTask(ProsessTaskTjeneste taskTjeneste,
                                             AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository,
                                             @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.taskTjeneste = taskTjeneste;
        this.automatiskSaksbehandlingRepository = automatiskSaksbehandlingRepository;
        this.clock = Clock.systemDefaultZone();
        this.grunnlagAlder = grunnlagAlder;
    }

    // kun for testbruk
    protected AutomatiskSaksbehandlingBatchTask(ProsessTaskTjeneste taskTjeneste,
                                                AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository,
                                                Clock clock,
                                                @KonfigVerdi(value = "automatisering.alder.kravgrunnlag") Period grunnlagAlder) {
        this.taskTjeneste = taskTjeneste;
        this.automatiskSaksbehandlingRepository = automatiskSaksbehandlingRepository;
        this.clock = clock;
        this.grunnlagAlder = grunnlagAlder;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String batchRun = this.getClass().getSimpleName() + EXECUTION_ID_SEPARATOR + UUID.randomUUID();
        LocalDate iDag = LocalDate.now(clock);
        DayOfWeek dagensUkedag = DayOfWeek.from(iDag);

        // Ingenting å kjøre i helger eller helligdager enn så lenge
        if (Helligdager.erHelligdagEllerHelg(iDag)) {
            logger.info("Kjører ikke batch {} i helgen eller helligdag. Iverksetting i saksbehandling avhenger av oppdragsystemet, som sannsynligvis har nedetid", batchRun);
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
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(AutomatiskSaksbehandlingProsessTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskData.setSekvens("10");
        prosessTaskData.setGruppe(getGruppeNavn(batchRun));
        taskTjeneste.lagre(prosessTaskData);
    }

    private String getGruppeNavn(String batchRun) {
        return batchRun.substring(batchRun.indexOf(EXECUTION_ID_SEPARATOR.charAt(0)) + 1);
    }
}
