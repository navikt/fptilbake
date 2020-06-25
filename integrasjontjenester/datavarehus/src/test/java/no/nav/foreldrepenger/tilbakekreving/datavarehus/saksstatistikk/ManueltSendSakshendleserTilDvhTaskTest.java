package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import javax.persistence.FlushModeType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.DvhEventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class ManueltSendSakshendleserTilDvhTaskTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private ProsessTaskRepository taskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, null);

    private BehandlingTilstandTjeneste tilstandTjeneste = new BehandlingTilstandTjeneste(repositoryProvider);
    private SakshendelserKafkaProducer kafkaProducerMock = Mockito.mock(SakshendelserKafkaProducer.class);
    private ManueltSendSakshendleserTilDvhTask manueltSendSakshendleserTilDvhTask =
        new ManueltSendSakshendleserTilDvhTask(kafkaProducerMock, tilstandTjeneste, behandlingRepository, taskRepository);

    private Behandling behandling;
    private ProsessTaskData prosessTaskData;

    @Before
    public void setup() {
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        ScenarioSimple scenarioSimple = ScenarioSimple.simple();
        behandling = scenarioSimple.lagre(repositoryProvider);
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), UUID.randomUUID());
        repositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);
        prosessTaskData = new ProsessTaskData(ManueltSendSakshendleserTilDvhTask.TASK_TYPE);
        prosessTaskData.setProperty("startDato","25.06.2020");
        prosessTaskData.setProperty("sisteDato","25.06.2020");
    }

    @Test
    public void skal_sende_behandling_opprettelse_sakshendelser_til_dvh() {
        prosessTaskData.setProperty("eventHendelse", DvhEventHendelse.AKSJONSPUNKT_OPPRETTET.name());
        manueltSendSakshendleserTilDvhTask.doTask(prosessTaskData);
        verify(kafkaProducerMock, Mockito.atLeastOnce()).sendMelding(any(BehandlingTilstand.class));
        fellesProsessTaskAssert();
    }

    @Test
    public void skal_sende_behandling_avsluttelse_sakshendelser_til_dvh() {
        behandling.avsluttBehandling();
        prosessTaskData.setProperty("eventHendelse", DvhEventHendelse.AKSJONSPUNKT_AVBRUTT.name());
        manueltSendSakshendleserTilDvhTask.doTask(prosessTaskData);
        verify(kafkaProducerMock, Mockito.atLeastOnce()).sendMelding(any(BehandlingTilstand.class));
        fellesProsessTaskAssert();
    }

    private void fellesProsessTaskAssert(){
        List<ProsessTaskData> prosesser = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosesser.size()).isEqualTo(1);
        prosessTaskData = prosesser.get(0);
        assertThat(prosessTaskData.getPropertyValue("antallBehandlinger")).isEqualTo("1");
    }
}
