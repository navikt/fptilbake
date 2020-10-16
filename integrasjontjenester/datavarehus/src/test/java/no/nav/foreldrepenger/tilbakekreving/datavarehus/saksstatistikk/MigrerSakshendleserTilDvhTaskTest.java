package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import javax.persistence.FlushModeType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.DvhEventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class MigrerSakshendleserTilDvhTaskTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(repositoryRule.getEntityManager());
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private ProsessTaskRepository taskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, null);

    private BehandlingTilstandTjeneste tilstandTjeneste = new BehandlingTilstandTjeneste(repositoryProvider);
    private SakshendelserKafkaProducer kafkaProducerMock = Mockito.mock(SakshendelserKafkaProducer.class);
    private MigrerSakshendleserTilDvhTask manueltSendSakshendleserTilDvhTask =
        new MigrerSakshendleserTilDvhTask(kafkaProducerMock, tilstandTjeneste, behandlingRepository, taskRepository);

    private Behandling behandling;
    private ProsessTaskData prosessTaskData;

    @Before
    public void setup() {
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
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
