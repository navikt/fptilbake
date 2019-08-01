package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.InternalAksjonspunktManipulator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class GjenopptaBehandlingTjenesteImplTest {

    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private InternalManipulerBehandling internalManipulerBehandling;

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;

    @Inject
    private BehandlingVenterRepository behandlingVenterRepository;

    @Inject
    private BehandlingKandidaterRepository behandlingKandidaterRepository;

    private InternalAksjonspunktManipulator internalAksjonspunktManipulator = new InternalAksjonspunktManipulator();
    private ProsessTaskRepository mockProsesstaskRepository = mock(ProsessTaskRepository.class);
    private VarselresponsTjeneste mockVarselResponsTjeneste = mock(VarselresponsTjeneste.class);

    @Before
    public void setup() {
        gjenopptaBehandlingTjeneste = new GjenopptaBehandlingTjenesteImpl(mockProsesstaskRepository,
                behandlingKandidaterRepository,
                behandlingVenterRepository,
                mockVarselResponsTjeneste);
    }

    @Test
    public void skal_lage_prosess_task_behandling() {
        final String gruppe = "66";

        Behandling behandling = lagBehandling();

        final AktørId aktørId = behandling.getAktørId();
        final Long fagsakId = behandling.getFagsakId();
        final Long behandlingId = behandling.getId();

        List<ProsessTaskData> faktiskProsesstaskDataliste = new ArrayList<>();
        prosessTaskCapture(gruppe, faktiskProsesstaskDataliste);

        gjenopptaBehandlingTjeneste.fortsettBehandling(behandlingId);

        assertThat(faktiskProsesstaskDataliste).hasSize(1);

        ProsessTaskData prosessTaskData = faktiskProsesstaskDataliste.get(0);
        assertThat(prosessTaskData.getAktørId()).isEqualTo(aktørId.getId());
        assertThat(prosessTaskData.getFagsakId()).isEqualTo(fagsakId);
        assertThat(prosessTaskData.getBehandlingId()).isEqualTo(behandlingId);
    }

    @Test
    public void skal_lage_prosess_tasks_for_behandlinger_som_skal_gjenopptas() {
        final String gruppe = "55";

        Behandling behandling1 = lagBehandling();
        Behandling behandling2 = lagBehandling();

        Long behandlingId1 = behandling1.getId();
        Long behandlingId2 = behandling2.getId();

        List<ProsessTaskData> faktiskeProsessTaskDataListe = new ArrayList<>();
        prosessTaskCapture(gruppe, faktiskeProsessTaskDataListe);

        // Act
        final String faktiskGruppe = gjenopptaBehandlingTjeneste.automatiskGjenopptaBehandlinger();

        assertThat(faktiskeProsessTaskDataListe).hasSize(2);
        ProsessTaskData prosessTaskData1 = faktiskeProsessTaskDataListe.get(0);
        ProsessTaskData prosessTaskData2 = faktiskeProsessTaskDataListe.get(1);

        List<Long> behandlingIderFraProsesstaskData = new ArrayList<>();
        behandlingIderFraProsesstaskData.add(prosessTaskData1.getBehandlingId());
        behandlingIderFraProsesstaskData.add(prosessTaskData2.getBehandlingId());

        assertThat(behandlingIderFraProsesstaskData).contains(behandlingId1, behandlingId2);
    }

    @Test
    public void skal_hente_statuser_for_gjenopptaBehandling_gruppe() {
        // Arrange
        final TaskStatus status1 = new TaskStatus(ProsessTaskStatus.FERDIG, new BigDecimal(1));
        final TaskStatus status2 = new TaskStatus(ProsessTaskStatus.FEILET, new BigDecimal(2));
        final List<TaskStatus> statusListFromRepo = Arrays.asList(status1, status2);
        when(mockProsesstaskRepository.finnStatusForTaskIGruppe(same(GjenopptaBehandlingTask.TASKTYPE), anyString())).thenReturn(statusListFromRepo);

        // Act
        List<TaskStatus> statusListFromSvc = gjenopptaBehandlingTjeneste.hentStatusForGjenopptaBehandlingGruppe("gruppa");

        // Assert
        assertThat(statusListFromSvc).containsExactly(status1, status2);
    }

    private Behandling lagBehandling() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().minusDays(10));
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER);
        BehandlingLås lås = behandlingRepositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        behandlingRepositoryProvider.getBehandlingRepository().lagre(behandling, lås);
        return behandling;
    }

    private void prosessTaskCapture(String gruppe, List<ProsessTaskData> faktiskeProsessTaskDataListe) {
        doAnswer((Answer<Void>) invocation -> {
            ProsessTaskData data = (ProsessTaskData) invocation.getArguments()[0];
            if (data.getGruppe() == null) {
                data.setGruppe(gruppe);
            }
            faktiskeProsessTaskDataListe.add(data);
            return null;
        }).when(mockProsesstaskRepository).lagre(any(ProsessTaskData.class));
    }
}
