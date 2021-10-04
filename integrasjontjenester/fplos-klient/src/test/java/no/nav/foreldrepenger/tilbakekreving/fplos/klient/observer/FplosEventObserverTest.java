package no.nav.foreldrepenger.tilbakekreving.fplos.klient.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktTilbakeførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktUtførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingManglerKravgrunnlagFristenEndretEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingManglerKravgrunnlagFristenUtløptEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.task.FplosPubliserEventTask;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class FplosEventObserverTest {

    private AksjonspunktRepository aksjonspunktRepository;

    private ProsessTaskTjeneste taskTjeneste;

    private BehandlingModell mockBehandlingModell;

    private FplosEventObserver fplosEventObserver;

    private InternalManipulerBehandling internalManipulerBehandling;

    private Behandling behandling;
    private BehandlingskontrollKontekst behandlingskontrollKontekst;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        BehandlingModellRepository mockBehandlingModellRepository = mock(BehandlingModellRepository.class);
        mockBehandlingModell = mock(BehandlingModell.class);
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(repositoryProvider,
            mockBehandlingModellRepository, mock(BehandlingskontrollEventPubliserer.class));
        fplosEventObserver = new FplosEventObserver(repositoryProvider.getBehandlingRepository(),
            taskTjeneste, behandlingskontrollTjeneste);
        internalManipulerBehandling = new InternalManipulerBehandling(repositoryProvider);

        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        behandlingskontrollKontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        when(mockBehandlingModellRepository.getModell(BehandlingType.TILBAKEKREVING)).thenReturn(mockBehandlingModell);
    }

    @Test
    public void skal_publisere_data_når_manuell_aksjonspunkt_er_opprettet() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
            BehandlingStegType.FAKTA_FEILUTBETALING);
        AksjonspunkterFunnetEvent aksjonspunkterFunnetEvent = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.FAKTA_FEILUTBETALING);

        fplosEventObserver.observerAksjonpunktFunnetEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    public void skal_publisere_data_for_autopunkter_når_behandling_er_i_fakta_steg() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunkterFunnetEvent aksjonspunkterFunnetEvent = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.FAKTA_FEILUTBETALING);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);

        fplosEventObserver.observerAksjonpunktFunnetEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    public void skal_publisere_data_for_autopunkter_når_behandling_er_i_vilkår_steg() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunkterFunnetEvent aksjonspunkterFunnetEvent = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VTILBSTEG);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.VTILBSTEG, BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        fplosEventObserver.observerAksjonpunktFunnetEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    public void skal_ikke_publisere_data_for_autopunkter_når_behandling_er_før_fakta_steg() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunkterFunnetEvent aksjonspunkterFunnetEvent = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VARSEL);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.VARSEL, BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(true);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        fplosEventObserver.observerAksjonpunktFunnetEvent(aksjonspunkterFunnetEvent);
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    public void skal_publisere_data_når_autopunkter_er_utført_og_behandling_er_i_fakta_steg() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
            BehandlingStegType.FAKTA_FEILUTBETALING);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunktUtførtEvent aksjonspunktUtførtEvent = new AksjonspunktUtførtEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.FAKTA_FEILUTBETALING);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);

        fplosEventObserver.observerAksjonpunktUtførtEvent(aksjonspunktUtførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_UTFØRT);
    }

    @Test
    public void skal_publisere_data_når_autopunkter_er_utført_og_behandling_er_forbi_fakta_steg() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VURDER_TILBAKEKREVING,
            BehandlingStegType.VTILBSTEG);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunktUtførtEvent aksjonspunktUtførtEvent = new AksjonspunktUtførtEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VTILBSTEG);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.VTILBSTEG, BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);

        fplosEventObserver.observerAksjonpunktUtførtEvent(aksjonspunktUtførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_UTFØRT);
    }

    @Test
    public void skal_ikke_publisere_data_når_autopunkter_er_utført_og_behandling_er_før_fakta_steg() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunktUtførtEvent aksjonspunktUtførtEvent = new AksjonspunktUtførtEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VARSEL);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.VARSEL, BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(true);

        fplosEventObserver.observerAksjonpunktUtførtEvent(aksjonspunktUtførtEvent);
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    public void skal_ikke_publisere_data_når_manuell_aksjonspunkter_er_utført_og_behandling_er_i_fakta_steg() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
            BehandlingStegType.FAKTA_FEILUTBETALING);
        AksjonspunktUtførtEvent aksjonspunktUtførtEvent = new AksjonspunktUtførtEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.FAKTA_FEILUTBETALING);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);

        fplosEventObserver.observerAksjonpunktUtførtEvent(aksjonspunktUtførtEvent);
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    public void skal_publisere_data_når_behandling_er_tilbakeført_til_fakta_steg() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
            BehandlingStegType.FAKTA_FEILUTBETALING);
        AksjonspunktTilbakeførtEvent aksjonspunktTilbakeførtEvent = new AksjonspunktTilbakeførtEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VTILBSTEG);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        fplosEventObserver.observerAksjonpunktTilbakeførtEvent(aksjonspunktTilbakeførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_TILBAKEFØR);
    }

    @Test
    public void skal_publisere_data_når_behandling_er_avsluttet() {
        BehandlingStatusEvent behandlingAvsluttetEvent = BehandlingStatusEvent.nyEvent(behandlingskontrollKontekst, BehandlingStatus.AVSLUTTET);

        fplosEventObserver.observerBehandlingAvsluttetEvent((BehandlingStatusEvent.BehandlingAvsluttetEvent) behandlingAvsluttetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_AVBRUTT);
    }

    @Test
    public void skal_publisere_data_når_behandling_enhet_er_byttet() {
        BehandlingEnhetEvent behandlingEnhetEvent = new BehandlingEnhetEvent(behandling);

        fplosEventObserver.observerAksjonspunktHarEndretBehandlendeEnhetEvent(behandlingEnhetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_HAR_ENDRET_BEHANDLENDE_ENHET);
    }

    @Test
    public void skal_publisere_data_når_behandling_sett_på_vent_og_fristen_er_utløpt() {
        LocalDateTime fristTid = LocalDateTime.now();
        BehandlingManglerKravgrunnlagFristenUtløptEvent utløptEvent = new BehandlingManglerKravgrunnlagFristenUtløptEvent(behandling, fristTid);

        fplosEventObserver.observerBehandlingFristenUtløptEvent(utløptEvent);
        ProsessTaskData publisherEventProsessTask = fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
        assertThat(publisherEventProsessTask.getPropertyValue(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID)).isEqualTo(fristTid.toString());
        assertThat(publisherEventProsessTask.getPropertyValue(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE)).isEqualTo(AksjonspunktStatus.OPPRETTET.getKode());
    }

    @Test
    public void skal_publisere_data_når_behandling_sett_på_vent_og_fristen_er_endret() {
        LocalDateTime fristTid = LocalDateTime.now();
        BehandlingManglerKravgrunnlagFristenEndretEvent fristenEndretEvent = new BehandlingManglerKravgrunnlagFristenEndretEvent(behandling,fristTid);

        fplosEventObserver.observerBehandlingFristenEndretEvent(fristenEndretEvent);
        ProsessTaskData publisherEventProsessTask = fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_AVBRUTT);
        assertThat(publisherEventProsessTask.getPropertyValue(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID)).isEqualTo(fristTid.toString());
        assertThat(publisherEventProsessTask.getPropertyValue(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE)).isEqualTo(AksjonspunktStatus.AVBRUTT.getKode());
    }

    private ProsessTaskData fellesAssertProsessTask(EventHendelse eventHendelse) {
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var publisherEventProsessTask = captor.getValue();
        assertThat(publisherEventProsessTask.taskType()).isEqualTo(TaskType.forProsessTask(FplosPubliserEventTask.class));
        assertThat(publisherEventProsessTask.getPropertyValue(FplosPubliserEventTask.PROPERTY_EVENT_NAME)).isEqualTo(eventHendelse.name());
        return publisherEventProsessTask;
    }
}
