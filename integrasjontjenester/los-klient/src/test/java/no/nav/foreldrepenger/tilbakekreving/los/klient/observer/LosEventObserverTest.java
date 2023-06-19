package no.nav.foreldrepenger.tilbakekreving.los.klient.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.AksjonspunktStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.los.klient.task.LosPubliserEventTask;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(JpaExtension.class)
class LosEventObserverTest {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private ProsessTaskTjeneste taskTjeneste;

    private LosEventObserver losEventObserver;

    private Behandling behandling;
    private BehandlingskontrollKontekst behandlingskontrollKontekst;

    @BeforeEach
    void setup(EntityManager entityManager) {
        var repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager,
                new BehandlingModellRepository(), mock(BehandlingskontrollEventPubliserer.class)));
        losEventObserver = new LosEventObserver(repositoryProvider.getBehandlingRepository(),
                taskTjeneste, behandlingskontrollTjeneste, Fagsystem.K9TILBAKE);

        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        behandlingskontrollKontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
    }

    @Test
    void skal_publisere_data_når_manuell_aksjonspunkt_er_opprettet() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.FAKTA_FEILUTBETALING, List.of(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
        var aksjonspunkterFunnetEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.FAKTA_FEILUTBETALING);

        losEventObserver.observerAksjonpunktStatusEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    void skal_publisere_data_for_autopunkter_når_behandling_er_i_fakta_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING));
        var aksjonspunkterFunnetEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
                BehandlingStegType.FAKTA_FEILUTBETALING);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);

        losEventObserver.observerAksjonpunktStatusEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    void skal_publisere_data_for_autopunkter_når_behandling_er_i_vilkår_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING));
        var aksjonspunkterFunnetEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.VTILBSTEG);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        losEventObserver.observerAksjonpunktStatusEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    void skal_ikke_publisere_data_for_autopunkter_når_behandling_er_før_fakta_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING));
        var aksjonspunkterFunnetEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.VARSEL);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        losEventObserver.observerAksjonpunktStatusEvent(aksjonspunkterFunnetEvent);
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void skal_publisere_data_når_autopunkter_er_utført_og_behandling_er_i_fakta_steg() {
        var apa = behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING)).get(0);
        behandlingskontrollTjeneste.lagreAksjonspunkterUtført(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(apa));
        var aksjonspunktUtførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, List.of(apa), BehandlingStegType.FAKTA_FEILUTBETALING);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        losEventObserver.observerAksjonpunktStatusEvent(aksjonspunktUtførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_UTFØRT);
    }

    @Test
    void skal_publisere_data_når_autopunkter_er_utført_og_behandling_er_forbi_fakta_steg() {
        var apa = behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING)).get(0);
        behandlingskontrollTjeneste.lagreAksjonspunkterUtført(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(apa));
        var aksjonspunktUtførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, List.of(apa), BehandlingStegType.VTILBSTEG);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        losEventObserver.observerAksjonpunktStatusEvent(aksjonspunktUtførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_UTFØRT);
    }

    @Test
    void skal_ikke_publisere_data_når_autopunkter_er_utført_og_behandling_er_før_fakta_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING)).get(0);

        var aksjonspunktUtførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.VARSEL);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        losEventObserver.observerAksjonpunktStatusEvent(aksjonspunktUtførtEvent);
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void skal_ikke_publisere_data_når_manuell_aksjonspunkter_er_utført_og_behandling_er_før_fakta_steg() {
        var ap = behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.FAKTA_VERGE, List.of(AksjonspunktDefinisjon.AVKLAR_VERGE)).get(0);
        behandlingskontrollTjeneste.lagreAksjonspunkterUtført(behandlingskontrollKontekst, BehandlingStegType.FAKTA_VERGE, List.of(ap));

        var aksjonspunktUtførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.FAKTA_VERGE);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_VERGE);

        losEventObserver.observerAksjonpunktStatusEvent(aksjonspunktUtførtEvent);
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void skal_publisere_data_når_behandling_er_tilbakeført_til_fakta_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.FAKTA_FEILUTBETALING, List.of(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
        var aksjonspunktTilbakeførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.VTILBSTEG);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        losEventObserver.observerAksjonpunktStatusEvent(aksjonspunktTilbakeførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    void skal_publisere_data_når_behandling_er_avsluttet() {
        var behandlingAvsluttetEvent = BehandlingStatusEvent.nyEvent(behandlingskontrollKontekst, BehandlingStatus.AVSLUTTET);

        losEventObserver.observerBehandlingAvsluttetEvent((BehandlingStatusEvent.BehandlingAvsluttetEvent) behandlingAvsluttetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_AVBRUTT);
    }

    @Test
    void skal_publisere_data_når_behandling_enhet_er_byttet() {
        var behandlingEnhetEvent = new BehandlingEnhetEvent(behandling);

        losEventObserver.observerAksjonspunktHarEndretBehandlendeEnhetEvent(behandlingEnhetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_HAR_ENDRET_BEHANDLENDE_ENHET);
    }

    private ProsessTaskData fellesAssertProsessTask(EventHendelse eventHendelse) {
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var publisherEventProsessTask = captor.getValue();
        assertThat(publisherEventProsessTask.taskType()).isEqualTo(TaskType.forProsessTask(LosPubliserEventTask.class));
        assertThat(publisherEventProsessTask.getPropertyValue(LosPubliserEventTask.PROPERTY_EVENT_NAME)).isEqualTo(eventHendelse.name());
        return publisherEventProsessTask;
    }
}
