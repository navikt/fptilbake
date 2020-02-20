package no.nav.foreldrepenger.tilbakekreving.fplos.klient.observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktTilbakeførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktUtførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.task.FplosPubliserEventTask;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class FplosEventObserverTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(),null,null);

    private BehandlingModellRepository mockBehandlingModellRepository =  mock(BehandlingModellRepository.class);
    private BehandlingModell mockBehandlingModell = mock(BehandlingModell.class);
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, mockBehandlingModellRepository,
        mock(BehandlingskontrollEventPubliserer.class));

    private FplosEventObserver fplosEventObserver = new FplosEventObserver(repositoryProvider.getBehandlingRepository(),prosessTaskRepository,behandlingskontrollTjeneste);

    private InternalManipulerBehandling internalManipulerBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);

    private Behandling behandling;
    private BehandlingskontrollKontekst behandlingskontrollKontekst;

    @Before
    public void setup(){
        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        behandlingskontrollKontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        when(mockBehandlingModellRepository.getModell(BehandlingType.TILBAKEKREVING)).thenReturn(mockBehandlingModell);
    }

    @Test
    public void skal_publisere_data_når_manuell_aksjonspunkt_er_oppretttet(){
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
            BehandlingStegType.FAKTA_FEILUTBETALING);
        AksjonspunkterFunnetEvent aksjonspunkterFunnetEvent = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst,behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.FAKTA_FEILUTBETALING);

        fplosEventObserver.observerAksjonpunktFunnetEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    public void skal_publisere_data_for_autopunkter_når_behandling_er_i_fakta_steg(){
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunkterFunnetEvent aksjonspunkterFunnetEvent = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst,behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.FAKTA_FEILUTBETALING);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.FAKTA_FEILUTBETALING,BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling,BehandlingStegType.FAKTA_FEILUTBETALING);

        fplosEventObserver.observerAksjonpunktFunnetEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    public void skal_publisere_data_for_autopunkter_når_behandling_er_i_vilkår_steg(){
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunkterFunnetEvent aksjonspunkterFunnetEvent = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst,behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VTILBSTEG);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.VTILBSTEG,BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling,BehandlingStegType.VTILBSTEG);

        fplosEventObserver.observerAksjonpunktFunnetEvent(aksjonspunkterFunnetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_OPPRETTET);
    }

    @Test
    public void skal_ikke_publisere_data_for_autopunkter_når_behandling_er_før_fakta_steg(){
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunkterFunnetEvent aksjonspunkterFunnetEvent = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst,behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VARSEL);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.VARSEL,BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(true);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling,BehandlingStegType.VARSEL);

        fplosEventObserver.observerAksjonpunktFunnetEvent(aksjonspunkterFunnetEvent);
        assertThat(prosessTaskRepository.finnIkkeStartet()).isEmpty();
    }

    @Test
    public void skal_publisere_data_når_autopunkter_er_utført_og_behandling_er_i_fakta_steg(){
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
            BehandlingStegType.FAKTA_FEILUTBETALING);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunktUtførtEvent aksjonspunktUtførtEvent = new AksjonspunktUtførtEvent(behandlingskontrollKontekst,behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.FAKTA_FEILUTBETALING);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling,BehandlingStegType.VTILBSTEG);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.FAKTA_FEILUTBETALING,BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);

        fplosEventObserver.observerAksjonpunktUtførtEvent(aksjonspunktUtførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_UTFØRT);
    }

    @Test
    public void skal_publisere_data_når_autopunkter_er_utført_og_behandling_er_forbi_fakta_steg(){
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VURDER_TILBAKEKREVING,
            BehandlingStegType.VTILBSTEG);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunktUtførtEvent aksjonspunktUtførtEvent = new AksjonspunktUtførtEvent(behandlingskontrollKontekst,behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VTILBSTEG);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling,BehandlingStegType.VTILBSTEG);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.VTILBSTEG,BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);

        fplosEventObserver.observerAksjonpunktUtførtEvent(aksjonspunktUtførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_UTFØRT);
    }

    @Test
    public void skal_ikke_publisere_data_når_autopunkter_er_utført_og_behandling_er_før_fakta_steg(){
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            BehandlingStegType.VARSEL);
        AksjonspunktUtførtEvent aksjonspunktUtførtEvent = new AksjonspunktUtførtEvent(behandlingskontrollKontekst,behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VARSEL);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling,BehandlingStegType.VARSEL);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.VARSEL,BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(true);

        fplosEventObserver.observerAksjonpunktUtførtEvent(aksjonspunktUtførtEvent);
        assertThat(prosessTaskRepository.finnIkkeStartet()).isEmpty();
    }

    @Test
    public void skal_ikke_publisere_data_når_manuell_aksjonspunkter_er_utført_og_behandling_er_i_fakta_steg(){
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
            BehandlingStegType.FAKTA_FEILUTBETALING);
        AksjonspunktUtførtEvent aksjonspunktUtførtEvent = new AksjonspunktUtførtEvent(behandlingskontrollKontekst,behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.FAKTA_FEILUTBETALING);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling,BehandlingStegType.FAKTA_FEILUTBETALING);
        when(mockBehandlingModell.erStegAFørStegB(BehandlingStegType.FAKTA_FEILUTBETALING,BehandlingStegType.FAKTA_FEILUTBETALING)).thenReturn(false);

        fplosEventObserver.observerAksjonpunktUtførtEvent(aksjonspunktUtførtEvent);
        assertThat(prosessTaskRepository.finnIkkeStartet()).isEmpty();
    }

    @Test
    public void skal_publisere_data_når_behandling_er_tilbakeført_til_fakta_steg(){
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
            BehandlingStegType.FAKTA_FEILUTBETALING);
        AksjonspunktTilbakeførtEvent aksjonspunktTilbakeførtEvent = new AksjonspunktTilbakeførtEvent(behandlingskontrollKontekst,behandling.getÅpneAksjonspunkter(),
            BehandlingStegType.VTILBSTEG);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling,BehandlingStegType.VTILBSTEG);

        fplosEventObserver.observerAksjonpunktTilbakeførtEvent(aksjonspunktTilbakeførtEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_TILBAKEFØR);
    }

    @Test
    public void skal_publisere_data_når_behandling_er_avsluttet(){
        BehandlingStatusEvent behandlingAvsluttetEvent = BehandlingStatusEvent.nyEvent(behandlingskontrollKontekst,BehandlingStatus.AVSLUTTET);

        fplosEventObserver.observerBehandlingAvsluttetEvent((BehandlingStatusEvent.BehandlingAvsluttetEvent) behandlingAvsluttetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_AVBRUTT);
    }

    @Test
    public void skal_publisere_data_når_behandling_enhet_er_byttet(){
        BehandlingEnhetEvent behandlingEnhetEvent = new BehandlingEnhetEvent(behandling);

        fplosEventObserver.observerAksjonspunktHarEndretBehandlendeEnhetEvent(behandlingEnhetEvent);
        fellesAssertProsessTask(EventHendelse.AKSJONSPUNKT_HAR_ENDRET_BEHANDLENDE_ENHET);
    }

    private void fellesAssertProsessTask(EventHendelse eventHendelse) {
        List<ProsessTaskData> prosessTasker = prosessTaskRepository.finnIkkeStartet();
        assertThat(prosessTasker.size()).isEqualTo(1);
        ProsessTaskData publisherEventProsessTask = prosessTasker.get(0);
        assertThat(publisherEventProsessTask.getTaskType()).isEqualTo(FplosPubliserEventTask.TASKTYPE);
        assertThat(publisherEventProsessTask.getPropertyValue(FplosPubliserEventTask.PROPERTY_EVENT_NAME)).isEqualTo(eventHendelse.name());
    }
}
