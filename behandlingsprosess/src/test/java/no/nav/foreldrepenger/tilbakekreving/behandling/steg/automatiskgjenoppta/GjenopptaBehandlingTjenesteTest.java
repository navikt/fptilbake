package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.InternalAksjonspunktManipulator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@CdiDbAwareTest
class GjenopptaBehandlingTjenesteTest {

    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;

    @Inject
    private BehandlingVenterRepository behandlingVenterRepository;

    @Inject
    private BehandlingKandidaterRepository behandlingKandidaterRepository;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    private ProsessTaskTjeneste mockTaskTjeneste = mock(ProsessTaskTjeneste.class);
    private InternalAksjonspunktManipulator internalAksjonspunktManipulator = new InternalAksjonspunktManipulator();

    @BeforeEach
    void setup() {
        gjenopptaBehandlingTjeneste = new GjenopptaBehandlingTjeneste(mockTaskTjeneste,
            behandlingKandidaterRepository,
            behandlingVenterRepository,
            repositoryProvider
        );
    }

    @Test
    void skal_lage_prosess_task_behandling() {
        final String gruppe = "66";

        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER,
            BehandlingStegStatus.VENTER);

        final Long fagsakId = behandling.getFagsakId();
        final Long behandlingId = behandling.getId();

        List<ProsessTaskData> faktiskProsesstaskDataliste = new ArrayList<>();
        prosessTaskCapture(gruppe, faktiskProsesstaskDataliste);

        gjenopptaBehandlingTjeneste.fortsettBehandling(behandlingId);

        assertThat(faktiskProsesstaskDataliste).hasSize(1);

        ProsessTaskData prosessTaskData = faktiskProsesstaskDataliste.get(0);
        assertThat(prosessTaskData.getSaksnummer()).isEqualTo(behandling.getSaksnummer().getVerdi());
        assertThat(prosessTaskData.getFagsakId()).isEqualTo(fagsakId);
        assertThat(prosessTaskData.getBehandlingIdAsLong()).isEqualTo(behandlingId);
    }

    @Test
    void skal_lage_forsett_behandling_prosess_task_når_behandling_er_manuelt_gjenopptatt() {
        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER,
            BehandlingStegStatus.VENTER);
        final Long behandlingId = behandling.getId();

        when(mockTaskTjeneste.lagre(any(ProsessTaskData.class))).thenReturn("Call_123");

        gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandlingId, behandling.getFagsakId(), HistorikkAktør.SAKSBEHANDLER);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        var historikkinnslag2List = repositoryProvider.getHistorikkinnslagRepository().hent(behandlingId);
        assertThat(historikkinnslag2List).hasSize(1);
        assertThat(historikkinnslag2List.get(0).getTittel()).isEqualTo("Behandlingen er gjenopptatt");
        assertThat(historikkinnslag2List.get(0).getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
    }

    @Test
    void skal_lage_prosess_tasks_for_behandlinger_som_skal_gjenopptas() {
        final String gruppe = "55";

        Behandling behandling1 = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling1, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER,
            BehandlingStegStatus.VENTER);
        Behandling behandling2 = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling2, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER,
            BehandlingStegStatus.VENTER);

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
        behandlingIderFraProsesstaskData.add(prosessTaskData1.getBehandlingIdAsLong());
        behandlingIderFraProsesstaskData.add(prosessTaskData2.getBehandlingIdAsLong());

        assertThat(behandlingIderFraProsesstaskData).contains(behandlingId1, behandlingId2);
    }

    @Test
    void skal_lage_prosess_tasks_for_behandlinger_med_aktiv_kravgrunnlag_som_skal_gjenopptas() {
        final String gruppe = "56";
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        scenario.medDefaultKravgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            LocalDateTime.now().minusDays(10));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER,
            BehandlingStegStatus.VENTER);

        List<ProsessTaskData> faktiskeProsessTaskDataListe = new ArrayList<>();
        prosessTaskCapture(gruppe, faktiskeProsessTaskDataListe);

        gjenopptaBehandlingTjeneste.automatiskGjenopptaBehandlinger();
        assertThat(faktiskeProsessTaskDataListe).hasSize(1);
    }

    @Test
    void skal_ikke_lage_prosess_tasks_for_behandlinger_med_sperret_kravgrunnlag_som_skal_gjenopptas() {
        final String gruppe = "56";
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        scenario.medDefaultKravgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            LocalDateTime.now().minusDays(10));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER,
            BehandlingStegStatus.VENTER);
        repositoryProvider.getGrunnlagRepository().sperrGrunnlag(behandling.getId());

        List<ProsessTaskData> faktiskeProsessTaskDataListe = new ArrayList<>();
        prosessTaskCapture(gruppe, faktiskeProsessTaskDataListe);

        gjenopptaBehandlingTjeneste.automatiskGjenopptaBehandlinger();
        assertThat(faktiskeProsessTaskDataListe).hasSize(0);
    }

    private Behandling lagBehandling() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            LocalDateTime.now().minusDays(10));
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
        }).when(mockTaskTjeneste).lagre(any(ProsessTaskData.class));
    }
}
