package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;


import static no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;

import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegTilstandSnapshot;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingStegOvergangEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.events.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;

@CdiDbAwareTest
class BehandlingskontrollEventPublisererTest {
    private final BehandlingType behandlingType = BehandlingType.TILBAKEKREVING;

    private static final BehandlingStegType STEG_1 = BehandlingStegType.FAKTA_VERGE;
    private static final BehandlingStegType STEG_2 = BehandlingStegType.FAKTA_FEILUTBETALING;
    private static final BehandlingStegType STEG_3 = BehandlingStegType.FORELDELSEVURDERINGSTEG;
    private static final BehandlingStegType STEG_4 = BehandlingStegType.VTILBSTEG;


    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private BehandlingskontrollServiceProvider serviceProvider;

    private BehandlingskontrollTjeneste kontrollTjeneste;

    @BeforeEach
    void setup() {
        System.setProperty("app.name", "fptilbake");
        BehandlingModellImpl behandlingModell = byggModell();

        kontrollTjeneste = new BehandlingskontrollTjeneste(serviceProvider) {
            @Override
            protected BehandlingModellImpl getModell(BehandlingType behandlingType) {
                return behandlingModell;
            }
        };

        TestEventObserver.startCapture();
    }

    @AfterEach
    void after() {
        TestEventObserver.reset();
        System.clearProperty("app.name");
    }

    @Test
    void skal_fyre_event_for_aksjonspunkt_funnet_ved_prosessering() {
        ScenarioSimple scenario = ScenarioSimple.simple();

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        var stegType = BehandlingStegType.FAKTA_FEILUTBETALING;

        var aksjonspunkt = serviceProvider.getAksjonspunktKontrollRepository().leggTilAksjonspunkt(behandling,
                AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, stegType);
        kontrollTjeneste.aksjonspunkterEndretStatus(kontekst, stegType, List.of(aksjonspunkt));

        var ads = new AksjonspunktDefinisjon[]{AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING};
        TestEventObserver.containsExactly(ads);
    }

    @Test
    void skal_fyre_event_for_behandlingskontroll_startet_stoppet_ved_prosessering() {
        // Arrange
        ScenarioSimple scenario = nyttScenario(STEG_1);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert

        BehandlingskontrollEvent startEvent = new BehandlingskontrollEvent.StartetEvent(null, null, STEG_1, null);
        BehandlingskontrollEvent stoppEvent = new BehandlingskontrollEvent.StoppetEvent(null, null, STEG_4, BehandlingStegStatus.UTGANG);
        TestEventObserver.containsExactly(startEvent, stoppEvent);

    }

    @Test
    void skal_fyre_event_for_behandlingskontroll_tilbakeføring_ved_prosessering() {
        // Arrange
        ScenarioSimple scenario = nyttScenario(STEG_3);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, STEG_4);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert
        // TODO (essv): Vanskelig å overstyre SUT til å gjøre tilbakehopp i riktig retning, her gjøres det fremover.
        // Den trenger et åpent aksjonspunkt som ligger før startsteget
        BehandlingStegOvergangEvent tilbakeføring3_4 = nyOvergangEvent(kontekst, STEG_3, BehandlingStegStatus.UTFØRT, STEG_4, null);
        TestEventObserver.containsExactly(tilbakeføring3_4);
    }

    @Test
    void skal_fyre_event_for_behandlingskontroll_behandlingsteg_overgang_ved_prosessering() {
        // Arrange
        ScenarioSimple scenario = nyttScenario(STEG_1);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert

        BehandlingStegOvergangEvent overgang1_2 = nyOvergangEvent(kontekst, STEG_1, BehandlingStegStatus.UTFØRT, STEG_2, null);
        BehandlingStegOvergangEvent overgang2_3 = nyOvergangEvent(kontekst, STEG_2, BehandlingStegStatus.UTFØRT, STEG_3, null);
        BehandlingStegOvergangEvent overgang3_4 = nyOvergangEvent(kontekst, STEG_3, BehandlingStegStatus.UTFØRT, STEG_4, null);
        TestEventObserver.containsExactly(overgang1_2, overgang2_3, overgang3_4);
    }

    @Test
    void skal_fyre_event_behandling_status_endring_ved_prosessering() {
        // Arrange
        var scenario = ScenarioSimple.simple();

        var behandling = scenario.lagre(repositoryProvider);

        var kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert

        var statusEvent = BehandlingStatusEvent.nyEvent(kontekst, BehandlingStatus.UTREDES);
        TestEventObserver.containsExactly(statusEvent);
    }

    protected ScenarioSimple nyttScenario(BehandlingStegType startSteg) {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingStegStart(startSteg);
        return scenario;
    }

    private BehandlingStegOvergangEvent nyOvergangEvent(BehandlingskontrollKontekst kontekst,
                                                        BehandlingStegType steg1, BehandlingStegStatus steg1Status, BehandlingStegType steg2, BehandlingStegStatus steg2Status) {
        return new BehandlingStegOvergangEvent(kontekst, lagTilstand(steg1, steg1Status),
                lagTilstand(steg2, steg2Status));
    }

    private BehandlingStegTilstandSnapshot lagTilstand(BehandlingStegType stegType,
                                                       BehandlingStegStatus stegStatus) {
        return new BehandlingStegTilstandSnapshot(1L, stegType, stegStatus);
    }

    private BehandlingModellImpl byggModell() {
        // Arrange - noen utvalge, tilfeldige aksjonspunkter
        var a0_0 = AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING;
        var a0_1 = AksjonspunktDefinisjon.AVKLAR_VERGE;
        var a1_1 = AksjonspunktDefinisjon.VURDER_FORELDELSE;
        var a2_1 = AksjonspunktDefinisjon.VURDER_TILBAKEKREVING;

        DummySteg steg = new DummySteg();
        DummySteg steg0 = new DummySteg(opprettForAksjonspunkt(a2_1));
        DummySteg steg1 = new DummySteg();
        DummySteg steg2 = new DummySteg();

        List<TestStegKonfig> modellData = List.of(
                new TestStegKonfig(STEG_1, behandlingType, steg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, steg0, ap(a0_0), ap(a0_1)),
                new TestStegKonfig(STEG_3, behandlingType, steg1, ap(), ap(a1_1)),
                new TestStegKonfig(STEG_4, behandlingType, steg2, ap(), ap(a2_1))
        );

        return ModifiserbarBehandlingModell.setupModell(behandlingType, modellData);
    }

    private List<AksjonspunktDefinisjon> ap(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        return Arrays.asList(aksjonspunktDefinisjoner);
    }
}
