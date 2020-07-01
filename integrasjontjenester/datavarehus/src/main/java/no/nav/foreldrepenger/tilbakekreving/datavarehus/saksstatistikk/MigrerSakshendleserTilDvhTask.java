package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import java.time.ZoneId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingResultat;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.DvhEventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ProsessTask(MigrerSakshendleserTilDvhTask.TASK_TYPE)
public class MigrerSakshendleserTilDvhTask implements ProsessTaskHandler {
    // Den prosess tasken bruker vi for å sende opprettelse og avsluttelse sakshendelser av alle behandlinger til Dvh i PROD.
    // Det brukes kun en gang i PROD så dvh kan motta alle dataene inntil nå. Denne prosess tasken kan gjøres kun manuelt
    // Denne kan utvides for alle andre sakshendler senere ved behov
    public static final String TASK_TYPE = "dvh.migrer.sakshendelser";

    private SakshendelserKafkaProducer kafkaProducer;
    private BehandlingTilstandTjeneste behandlingTilstandTjeneste;
    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository taskRepository;

    @Inject
    public MigrerSakshendleserTilDvhTask(SakshendelserKafkaProducer kafkaProducer,
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
        Long behandlingId = Long.valueOf(prosessTaskData.getPropertyValue("behandlingId"));
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BehandlingTilstand behandlingTilstand = null;
        if (DvhEventHendelse.AKSJONSPUNKT_OPPRETTET.equals(eventHendelse)) {
            behandlingTilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(behandling.getId());
            behandlingTilstand.setFunksjonellTid(behandling.getOpprettetTidspunkt().atZone(ZoneId.of("UTC")).toOffsetDateTime());
            behandlingTilstand.setBehandlingStatus(BehandlingStatus.OPPRETTET);
            behandlingTilstand.setBehandlingResultat(BehandlingResultat.IKKE_FASTSATT);
        } else if (DvhEventHendelse.AKSJONSPUNKT_AVBRUTT.equals(eventHendelse)) {
            behandlingTilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(behandling.getId());
            behandlingTilstand.setFunksjonellTid(behandling.getAvsluttetDato().atZone(ZoneId.of("UTC")).toOffsetDateTime());
        }
        prosessTaskData.setPayload(BehandlingTilstandMapper.tilJsonString(behandlingTilstand));
        taskRepository.lagre(prosessTaskData);
        kafkaProducer.sendMelding(behandlingTilstand);
    }
}
