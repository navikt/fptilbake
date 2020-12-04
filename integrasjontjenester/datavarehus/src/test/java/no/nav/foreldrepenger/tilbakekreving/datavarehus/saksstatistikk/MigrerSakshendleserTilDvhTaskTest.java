package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.DvhEventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class MigrerSakshendleserTilDvhTaskTest {

    private SakshendelserKafkaProducer kafkaProducerMock;
    private MigrerSakshendleserTilDvhTask manueltSendSakshendleserTilDvhTask;

    private Behandling behandling;
    private ProsessTaskData prosessTaskData;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(
            entityManager);
        BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
        ProsessTaskRepository taskRepository = new ProsessTaskRepositoryImpl(entityManager, null,
            null);
        BehandlingTilstandTjeneste tilstandTjeneste = new BehandlingTilstandTjeneste(repositoryProvider);
        kafkaProducerMock = Mockito.mock(SakshendelserKafkaProducer.class);
        manueltSendSakshendleserTilDvhTask = new MigrerSakshendleserTilDvhTask(kafkaProducerMock, tilstandTjeneste,
            behandlingRepository, taskRepository);

        entityManager.setFlushMode(FlushModeType.AUTO);
        ScenarioSimple scenarioSimple = ScenarioSimple.simple();
        behandling = scenarioSimple.lagre(repositoryProvider);
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), UUID.randomUUID());
        repositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);
        prosessTaskData = new ProsessTaskData(MigrerSakshendleserTilDvhTask.TASK_TYPE);
        prosessTaskData.setProperty("behandlingId",String.valueOf(behandling.getId()));
    }

    @Test
    public void skal_sende_behandling_opprettelse_sakshendelser_til_dvh() {
        prosessTaskData.setProperty("eventHendelse", DvhEventHendelse.AKSJONSPUNKT_OPPRETTET.name());
        manueltSendSakshendleserTilDvhTask.doTask(prosessTaskData);
        verify(kafkaProducerMock, Mockito.atLeastOnce()).sendMelding(any(BehandlingTilstand.class));
        assertThat(prosessTaskData.getPayloadAsString()).isNotEmpty();
    }

    @Test
    public void skal_sende_behandling_avsluttelse_sakshendelser_til_dvh() {
        behandling.avsluttBehandling();
        prosessTaskData.setProperty("eventHendelse", DvhEventHendelse.AKSJONSPUNKT_AVBRUTT.name());
        manueltSendSakshendleserTilDvhTask.doTask(prosessTaskData);
        verify(kafkaProducerMock, Mockito.atLeastOnce()).sendMelding(any(BehandlingTilstand.class));
        assertThat(prosessTaskData.getPayloadAsString()).isNotEmpty();
    }

}
