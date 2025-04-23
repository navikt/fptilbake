package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.automatisksaksbehandling.AutomatiskSaksbehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;
import no.nav.foreldrepenger.tilbakekreving.felles.Helligdager;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "batch.automatisk.saksbehandling", prioritet = 2, cronExpression = "0 30 7 ? * MON-FRI")
public class AutomatiskSaksbehandlingBatchTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AutomatiskSaksbehandlingBatchTask.class);
    private static final String EXECUTION_ID_SEPARATOR = "-";

    private final ProsessTaskTjeneste taskTjeneste;
    private final AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository;
    private final KravgrunnlagRepository grunnlagRepository;
    private Clock clock;

    @Inject
    public AutomatiskSaksbehandlingBatchTask(ProsessTaskTjeneste taskTjeneste,
                                             AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository,
                                             KravgrunnlagRepository grunnlagRepository) {
        this(taskTjeneste, automatiskSaksbehandlingRepository, grunnlagRepository, Clock.systemDefaultZone());
    }

    // kun for testbruk
    protected AutomatiskSaksbehandlingBatchTask(ProsessTaskTjeneste taskTjeneste,
                                                AutomatiskSaksbehandlingRepository automatiskSaksbehandlingRepository,
                                                KravgrunnlagRepository grunnlagRepository,
                                                Clock clock) {
        this.taskTjeneste = taskTjeneste;
        this.automatiskSaksbehandlingRepository = automatiskSaksbehandlingRepository;
        this.grunnlagRepository = grunnlagRepository;
        this.clock = clock;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String batchRun = this.getClass().getSimpleName() + EXECUTION_ID_SEPARATOR + UUID.randomUUID();
        LocalDate iDag = LocalDate.now(clock);

        // Ingenting å kjøre i helger eller helligdager enn så lenge
        if (Helligdager.erHelligdagEllerHelg(iDag)) {
            LOG.info("Kjører ikke batch {} i helgen eller helligdag. Iverksetting i saksbehandling avhenger av oppdragsystemet, som sannsynligvis har nedetid", batchRun);
        } else {
            LocalDate loggDato = iDag.minus(Frister.KRAVGRUNNLAG_ALDER_GAMMELT);
            LOG.info("Henter behandlinger som er eldre enn {} i batch {}", loggDato, batchRun);
            List<Behandling> behandlinger = automatiskSaksbehandlingRepository.hentAlleBehandlingerSomErKlarForAutomatiskSaksbehandling(iDag);
            var opprettTaskForBehandlinger  = behandlinger.stream()
                .filter(behandling -> !behandling.isBehandlingPåVent())
                .filter(this::lavFeilutbetalingSomHarVentetKanBehandlesAutomatisk)
                .toList();
            LOG.info("Det finnes {} behandlinger som er klar for automatisk saksbehandling", opprettTaskForBehandlinger.size());
            opprettTaskForBehandlinger.forEach(behandling -> opprettAutomatiskSaksbehandlingProsessTask(batchRun, behandling));
        }
    }

    private void opprettAutomatiskSaksbehandlingProsessTask(String batchRun, Behandling behandling) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(AutomatiskSaksbehandlingProsessTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setSekvens("10");
        prosessTaskData.setGruppe(getGruppeNavn(batchRun));
        taskTjeneste.lagre(prosessTaskData);
    }

    private String getGruppeNavn(String batchRun) {
        return batchRun.substring(batchRun.indexOf(EXECUTION_ID_SEPARATOR.charAt(0)) + 1);
    }


    // Sikre match for aktuelt rettsgebyr
    private boolean lavFeilutbetalingSomHarVentetKanBehandlesAutomatisk(Behandling behandling) {
        var kravgrunnlag = grunnlagRepository.finnKravgrunnlagOpt(behandling.getId()).orElse(null);
        return kravgrunnlag != null && KravgrunnlagBeregningTjeneste.samletFeilutbetaltKanAutomatiskBehandles(kravgrunnlag, behandling.getOpprettetTidspunkt());
    }
}
