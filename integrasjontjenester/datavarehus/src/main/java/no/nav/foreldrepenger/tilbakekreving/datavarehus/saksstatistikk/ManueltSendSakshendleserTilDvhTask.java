package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingResultat;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.DvhEventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ProsessTask(ManueltSendSakshendleserTilDvhTask.TASK_TYPE)
public class ManueltSendSakshendleserTilDvhTask implements ProsessTaskHandler {
    // Den prosess tasken bruker vi for å sende opprettelse og avsluttelse sakshendelser av alle behandlinger til Dvh i PROD.
    // Det brukes kun en gang i PROD så dvh kan motta alle dataene inntil nå. Denne prosess tasken kan gjøres kun manuelt
    // Denne kan utvides for alle andre sakshendler senere ved behov
    public static final String TASK_TYPE = "dvh.send.sakshendelser.manuelt";

    private SakshendelserKafkaProducer kafkaProducer;
    private BehandlingTilstandTjeneste behandlingTilstandTjeneste;
    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository taskRepository;

    @Inject
    public ManueltSendSakshendleserTilDvhTask(SakshendelserKafkaProducer kafkaProducer,
                                              BehandlingTilstandTjeneste behandlingTilstandTjeneste,
                                              BehandlingRepository behandlingRepository,
                                              ProsessTaskRepository taskRepository) {
        this.kafkaProducer = kafkaProducer;
        this.behandlingTilstandTjeneste = behandlingTilstandTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        DvhEventHendelse eventHendelse = DvhEventHendelse.valueOf(prosessTaskData.getPropertyValue("eventHendelse"));
        LocalDate førsteDato = LocalDate.parse(prosessTaskData.getPropertyValue("startDato"), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalDate sisteDato =  LocalDate.parse(prosessTaskData.getPropertyValue("sisteDato"), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        int antallBehandlinger = 0;
        if (DvhEventHendelse.AKSJONSPUNKT_OPPRETTET.equals(eventHendelse)) {
            List<Behandling> behandlinger = behandlingRepository.hentAlleBehandlinger(førsteDato,sisteDato);
            for (Behandling behandling : behandlinger) {
                BehandlingTilstand behandlingTilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(behandling.getId());
                behandlingTilstand.setFunksjonellTid(behandling.getOpprettetTidspunkt().atZone(ZoneId.of("UTC")).toOffsetDateTime());
                behandlingTilstand.setBehandlingStatus(BehandlingStatus.OPPRETTET);
                behandlingTilstand.setBehandlingResultat(BehandlingResultat.IKKE_FASTSATT);
                kafkaProducer.sendMelding(behandlingTilstand);
                antallBehandlinger ++;
            }
        } else if (DvhEventHendelse.AKSJONSPUNKT_AVBRUTT.equals(eventHendelse)) {
            List<Behandling> behandlinger = behandlingRepository.hentAlleAvsluttetBehandlinger(førsteDato,sisteDato);
            for (Behandling behandling : behandlinger) {
                BehandlingTilstand behandlingTilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(behandling.getId());
                behandlingTilstand.setFunksjonellTid(behandling.getAvsluttetDato().atZone(ZoneId.of("UTC")).toOffsetDateTime());
                kafkaProducer.sendMelding(behandlingTilstand);
                antallBehandlinger ++;
            }
        }
        oppdaterProsessTask(prosessTaskData, antallBehandlinger);
    }

    private void oppdaterProsessTask(ProsessTaskData prosessTaskData, int antallBehandlinger) {
        prosessTaskData.setProperty("antallBehandlinger", String.valueOf(antallBehandlinger));
        taskRepository.lagre(prosessTaskData);
    }

}
