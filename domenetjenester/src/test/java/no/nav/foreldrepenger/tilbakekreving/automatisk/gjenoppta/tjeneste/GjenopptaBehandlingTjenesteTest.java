package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@CdiDbAwareTest
public class GjenopptaBehandlingTjenesteTest {

    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    @Inject
    private BehandlingRepositoryProvider behandlingRepositoryProvider;

    @Inject
    private BehandlingVenterRepository behandlingVenterRepository;

    @Inject
    private BehandlingKandidaterRepository behandlingKandidaterRepository;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    private InternalAksjonspunktManipulator internalAksjonspunktManipulator = new InternalAksjonspunktManipulator();
    private ProsessTaskTjeneste mockTaskTjeneste = mock(ProsessTaskTjeneste.class);
    private VarselresponsTjeneste mockVarselResponsTjeneste = mock(VarselresponsTjeneste.class);

    @BeforeEach
    public void setup() {
        gjenopptaBehandlingTjeneste = new GjenopptaBehandlingTjeneste(mockTaskTjeneste,
            behandlingKandidaterRepository,
            behandlingVenterRepository,
            repositoryProvider,
            mockVarselResponsTjeneste);
    }

    @Test
    public void skal_lage_prosess_task_behandling() {
        final String gruppe = "66";

        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);

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
        assertThat(prosessTaskData.getBehandlingId()).isEqualTo(Long.toString(behandlingId));
    }

    @Test
    public void skal_lage_forsett_behandling_prosess_task_når_behandling_er_manuelt_gjenopptatt() {
        final String gruppe = "44";
        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);
        final Long behandlingId = behandling.getId();

        when(mockTaskTjeneste.lagre(any(ProsessTaskData.class))).thenReturn("Call_123");

        gjenopptaBehandlingTjeneste.fortsettBehandlingManuelt(behandlingId, HistorikkAktør.SAKSBEHANDLER);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        List<Historikkinnslag> historikkinnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandlingId);
        assertThat(historikkinnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkinnslager.get(0);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BEH_MAN_GJEN);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
    }

    @Test
    public void skal_lage_prosess_tasks_for_behandlinger_som_skal_gjenopptas() {
        final String gruppe = "55";

        Behandling behandling1 = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling1, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);
        Behandling behandling2 = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling2, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);

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
        behandlingIderFraProsesstaskData.add(Long.parseLong(prosessTaskData1.getBehandlingId()));
        behandlingIderFraProsesstaskData.add(Long.parseLong(prosessTaskData2.getBehandlingId()));

        assertThat(behandlingIderFraProsesstaskData).contains(behandlingId1, behandlingId2);
    }

    @Test
    public void skal_lage_prosess_tasks_for_behandlinger_med_aktiv_kravgrunnlag_som_skal_gjenopptas() {
        final String gruppe = "56";
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        scenario.medDefaultKravgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().minusDays(10));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);

        List<ProsessTaskData> faktiskeProsessTaskDataListe = new ArrayList<>();
        prosessTaskCapture(gruppe, faktiskeProsessTaskDataListe);

        gjenopptaBehandlingTjeneste.automatiskGjenopptaBehandlinger();
        assertThat(faktiskeProsessTaskDataListe).hasSize(1);
    }

    @Test
    public void skal_ikke_lage_prosess_tasks_for_behandlinger_med_sperret_kravgrunnlag_som_skal_gjenopptas() {
        final String gruppe = "56";
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        scenario.medDefaultKravgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().minusDays(10));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);
        repositoryProvider.getGrunnlagRepository().sperrGrunnlag(behandling.getId());

        List<ProsessTaskData> faktiskeProsessTaskDataListe = new ArrayList<>();
        prosessTaskCapture(gruppe, faktiskeProsessTaskDataListe);

        gjenopptaBehandlingTjeneste.automatiskGjenopptaBehandlinger();
        assertThat(faktiskeProsessTaskDataListe).hasSize(0);
    }

    @Test
    public void skal_ikke_fortsette_behandling_med_grunnlag_for_behandling_i_varsel_steg_og_fristen_ikke_gått_ut() {
        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().plusDays(20));
        when(mockTaskTjeneste.lagre(any(ProsessTaskData.class))).thenReturn("Call_123");
        Optional<String> callId = gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandling.getId());
        assertThat(callId).isEmpty();
    }

    @Test
    public void skal_fortsette_behandling_med_grunnlag_for_behandling_i_tbk_steg() {
        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(20));
        when(mockTaskTjeneste.lagre(any(ProsessTaskData.class))).thenReturn("Call_123");
        Optional<String> callId = gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandling.getId());
        assertThat(callId).isNotEmpty();
    }

    @Test
    public void skal_fortsette_behandling_med_grunnlag_for_behandling_i_fakta_steg() {
        Behandling behandling = lagBehandling();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStegStatus.VENTER, BehandlingStegStatus.VENTER);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, LocalDateTime.now().plusDays(20));
        when(mockTaskTjeneste.lagre(any(ProsessTaskData.class))).thenReturn("Call_123");
        Optional<String> callId = gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandling.getId());
        assertThat(callId).isNotEmpty();
    }

    private Behandling lagBehandling() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingType(BehandlingType.TILBAKEKREVING);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        Behandling behandling = scenario.lagre(behandlingRepositoryProvider);
        internalAksjonspunktManipulator.forceFristForAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, LocalDateTime.now().minusDays(10));
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
