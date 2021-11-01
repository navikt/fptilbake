package no.nav.foreldrepenger.tilbakekreving.fplos.klient.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingManglerKravgrunnlagFristenEndretEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingManglerKravgrunnlagFristenUtløptEvent;
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

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private ProsessTaskTjeneste taskTjeneste;

    private FplosEventObserver fplosEventObserver;

    private Behandling behandling;
    private BehandlingskontrollKontekst behandlingskontrollKontekst;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager,
            new BehandlingModellRepository(), mock(BehandlingskontrollEventPubliserer.class)));
        fplosEventObserver = new FplosEventObserver(repositoryProvider.getBehandlingRepository(),
            taskTjeneste, behandlingskontrollTjeneste);

        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        behandlingskontrollKontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
    }

    @Test
    public void skal_publisere_data_når_manuell_aksjonspunkt_er_opprettet() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.FAKTA_FEILUTBETALING, List.of(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
        var aksjonspunkterFunnetEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),BehandlingStegType.FAKTA_FEILUTBETALING);

        fplosEventObserver.observerAksjonpunktStatusEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    public void skal_publisere_data_for_autopunkter_når_behandling_er_i_fakta_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING));
        var aksjonspunkterFunnetEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.FAKTA_FEILUTBETALING);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);

        fplosEventObserver.observerAksjonpunktStatusEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    public void skal_publisere_data_for_autopunkter_når_behandling_er_i_vilkår_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING));
        var aksjonspunkterFunnetEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.VTILBSTEG);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        fplosEventObserver.observerAksjonpunktStatusEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    public void skal_ikke_publisere_data_for_autopunkter_når_behandling_er_før_fakta_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING));
        var aksjonspunkterFunnetEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.VARSEL);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        fplosEventObserver.observerAksjonpunktStatusEvent(aksjonspunkterFunnetEvent);
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    public void skal_publisere_data_når_autopunkter_er_utført_og_behandling_er_i_fakta_steg() {
        var apa = behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING)).get(0);
        behandlingskontrollTjeneste.lagreAksjonspunkterUtført(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(apa));
        var aksjonspunktUtførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, List.of(apa), BehandlingStegType.FAKTA_FEILUTBETALING);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        fplosEventObserver.observerAksjonpunktStatusEvent(aksjonspunktUtførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_UTFØRT);
    }

    @Test
    public void skal_publisere_data_når_autopunkter_er_utført_og_behandling_er_forbi_fakta_steg() {
        var apa = behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING)).get(0);
        behandlingskontrollTjeneste.lagreAksjonspunkterUtført(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(apa));
        var aksjonspunktUtførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, List.of(apa), BehandlingStegType.VTILBSTEG);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        fplosEventObserver.observerAksjonpunktStatusEvent(aksjonspunktUtførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_UTFØRT);
    }

    @Test
    public void skal_ikke_publisere_data_når_autopunkter_er_utført_og_behandling_er_før_fakta_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING)).get(0);

        var aksjonspunktUtførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.VARSEL);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        fplosEventObserver.observerAksjonpunktStatusEvent(aksjonspunktUtførtEvent);
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    public void skal_ikke_publisere_data_når_manuell_aksjonspunkter_er_utført_og_behandling_er_før_fakta_steg() {
        var ap = behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.FAKTA_VERGE, List.of(AksjonspunktDefinisjon.AVKLAR_VERGE)).get(0);
        behandlingskontrollTjeneste.lagreAksjonspunkterUtført(behandlingskontrollKontekst, BehandlingStegType.FAKTA_VERGE, List.of(ap));

        var aksjonspunktUtførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.FAKTA_VERGE);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_VERGE);

        fplosEventObserver.observerAksjonpunktStatusEvent(aksjonspunktUtførtEvent);
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    public void skal_publisere_data_når_behandling_er_tilbakeført_til_fakta_steg() {
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(behandlingskontrollKontekst, BehandlingStegType.FAKTA_FEILUTBETALING, List.of(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
        var aksjonspunktTilbakeførtEvent = new AksjonspunktStatusEvent(behandlingskontrollKontekst, behandling.getÅpneAksjonspunkter(), BehandlingStegType.VTILBSTEG);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);

        fplosEventObserver.observerAksjonpunktStatusEvent(aksjonspunktTilbakeførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
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
