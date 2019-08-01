package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;


import static no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegOvergangEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BehandlingskontrollEventPublisererTest {
    private final BehandlingType behandlingType = BehandlingType.TILBAKEKREVING;

    private static final BehandlingStegType STEG_1 = TestBehandlingStegType.STEG_1;
    private static final BehandlingStegType STEG_2 = TestBehandlingStegType.STEG_2;
    private static final BehandlingStegType STEG_3 = TestBehandlingStegType.STEG_3;
    private static final BehandlingStegType STEG_4 = TestBehandlingStegType.STEG_4;

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private EntityManager em = repoRule.getEntityManager();

    @Inject
    BehandlingskontrollEventPubliserer eventPubliserer;

    @Inject
    BehandlingRepositoryProvider repositoryProvider;

    @Inject
    BehandlingModellRepository behandlingModellRepository;

    @Inject
    AksjonspunktRepository aksjonspunktRepository;

    // No Inject
    BehandlingskontrollTjenesteImpl kontrollTjeneste;

    @Before
    public void setup() {
        opprettStatiskModell();

        BehandlingModellImpl behandlingModell = byggModell();

        kontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, behandlingModellRepository, eventPubliserer) {
            @Override
            protected BehandlingModellImpl getModell(Behandling behandling) {
                return behandlingModell;
            }
        };

        TestEventObserver.startCapture();
    }

    @After
    public void after() {
        TestEventObserver.reset();
    }

    @Test
    public void skal_fyre_event_for_aksjonspunkt_funnet_ved_prosessering() throws Exception {
        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, TestAksjonspunktDefinisjon.AP_1, STEG_1);
        kontrollTjeneste.aksjonspunkterFunnet(kontekst, STEG_1, Arrays.asList(aksjonspunkt));

        AksjonspunktDefinisjon[] ads = {TestAksjonspunktDefinisjon.AP_1};
        TestEventObserver.containsExactly(ads);
    }

    @Test
    public void skal_fyre_event_for_behandlingskontroll_startet_stoppet_ved_prosessering() throws Exception {
        // Arrange
        ScenarioSimple scenario = nyttScenario(STEG_1);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert

        BehandlingskontrollEvent startEvent = new BehandlingskontrollEvent.StartetEvent(null, null, STEG_1, null);
        BehandlingskontrollEvent stoppEvent = new BehandlingskontrollEvent.StoppetEvent(null, null, STEG_4, BehandlingStegStatus.INNGANG);
        TestEventObserver.containsExactly(startEvent, stoppEvent);

    }

    @Test
    public void skal_fyre_event_for_behandlingskontroll_behandlingsteg_status_endring_ved_prosessering() throws Exception {
        // Arrange
        ScenarioSimple scenario = nyttScenario(STEG_1);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert

        BehandlingStegStatusEvent steg1StatusEvent0 = new BehandlingStegStatusEvent(kontekst, STEG_1, null,
            BehandlingStegStatus.STARTET);
        BehandlingStegStatusEvent steg1StatusEvent1 = new BehandlingStegStatusEvent(kontekst, STEG_1, BehandlingStegStatus.STARTET,
            BehandlingStegStatus.UTFØRT);
        BehandlingStegStatusEvent steg2StatusEvent0 = new BehandlingStegStatusEvent(kontekst, STEG_2, null,
            BehandlingStegStatus.STARTET);
        BehandlingStegStatusEvent steg2StatusEvent = new BehandlingStegStatusEvent(kontekst, STEG_2, BehandlingStegStatus.STARTET,
            BehandlingStegStatus.UTFØRT);
        BehandlingStegStatusEvent steg3StatusEvent0 = new BehandlingStegStatusEvent(kontekst, STEG_2, null,
            BehandlingStegStatus.STARTET);
        BehandlingStegStatusEvent steg3StatusEvent = new BehandlingStegStatusEvent(kontekst, STEG_3, BehandlingStegStatus.STARTET,
            BehandlingStegStatus.UTFØRT);
        BehandlingStegStatusEvent steg4StatusEvent = new BehandlingStegStatusEvent(kontekst, STEG_4, null,
            BehandlingStegStatus.INNGANG);
        TestEventObserver.containsExactly(steg1StatusEvent0, steg1StatusEvent1 //
            , steg2StatusEvent0, steg2StatusEvent//
            , steg3StatusEvent0, steg3StatusEvent//
            , steg4StatusEvent//
        );
    }

    @Test
    public void skal_fyre_event_for_behandlingskontroll_tilbakeføring_ved_prosessering() throws Exception {
        // Arrange
        ScenarioSimple scenario = nyttScenario(STEG_3);
        scenario.leggTilAksjonspunkt(TestAksjonspunktDefinisjon.AP_5, STEG_4);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert
        // TODO (essv): Vanskelig å overstyre SUT til å gjøre tilbakehopp i riktig retning, her gjøres det fremover.
        // Den trenger et åpent aksjonspunkt som ligger før startsteget
        BehandlingStegOvergangEvent tilbakeføring3_4 = nyOvergangEvent(kontekst, behandling, STEG_3, BehandlingStegStatus.UTFØRT, STEG_4, BehandlingStegStatus.UTFØRT);
        TestEventObserver.containsExactly(tilbakeføring3_4);
    }

    @Test
    public void skal_fyre_event_for_behandlingskontroll_behandlingsteg_overgang_ved_prosessering() throws Exception {
        // Arrange
        ScenarioSimple scenario = nyttScenario(STEG_1);

        Behandling behandling = scenario.lagre(repositoryProvider);

        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling.getId());

        // Act
        kontrollTjeneste.prosesserBehandling(kontekst);

        // Assert

        BehandlingStegOvergangEvent overgang1_2 = nyOvergangEvent(kontekst, behandling, STEG_1, BehandlingStegStatus.UTFØRT, STEG_2, BehandlingStegStatus.UTFØRT);
        BehandlingStegOvergangEvent overgang2_3 = nyOvergangEvent(kontekst, behandling, STEG_2, BehandlingStegStatus.UTFØRT, STEG_3, BehandlingStegStatus.UTFØRT);
        BehandlingStegOvergangEvent overgang3_4 = nyOvergangEvent(kontekst, behandling, STEG_3, BehandlingStegStatus.UTFØRT, STEG_4, BehandlingStegStatus.UTFØRT);
        TestEventObserver.containsExactly(overgang1_2, overgang2_3, overgang3_4);
    }

    protected ScenarioSimple nyttScenario(BehandlingStegType startSteg) {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.medBehandlingStegStart(startSteg);
        return scenario;
    }

    private BehandlingStegOvergangEvent nyOvergangEvent(BehandlingskontrollKontekst kontekst, Behandling behandling,
                                                        BehandlingStegType steg1, BehandlingStegStatus steg1Status, BehandlingStegType steg2, BehandlingStegStatus steg2Status) {
        return new BehandlingStegOvergangEvent(kontekst, lagTilstand(behandling, steg1, steg1Status),
            lagTilstand(behandling, steg2, steg2Status));
    }

    private Optional<BehandlingStegTilstand> lagTilstand(Behandling behandling, BehandlingStegType stegType,
                                                         BehandlingStegStatus stegStatus) {
        return Optional.of(new BehandlingStegTilstand(behandling, stegType, stegStatus));
    }

    @SuppressWarnings("Duplicates")
    private void opprettStatiskModell() {
        sql("INSERT INTO KODELISTE (id, kodeverk, kode, ekstra_data) values (seq_kodeliste.nextval, 'BEHANDLING_TYPE', 'BT-TEST2', '{behandlingstidFristUker: 3}')");

        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-1', 'test-steg-1', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-2', 'test-steg-2', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-3', 'test-steg-3', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-4', 'test-steg-4', 'UTRED', 'test')");

        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-2.INN', 'STEG-2', 'INN', 'STEG-2.INN')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-3.INN', 'STEG-3', 'INN', 'STEG-3.INN')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-4.INN', 'STEG-4', 'INN', 'STEG-4.INN')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-2.UT', 'STEG-2', 'UT', 'STEG-2.UT')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-3.UT', 'STEG-3', 'UT', 'STEG-3.UT')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-4.UT', 'STEG-4', 'UT', 'STEG-4.UT')");

        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_1', 'AP 1', 'STEG-2.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_2', 'AP_2', 'STEG-2.UT', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_3', 'AP_3', 'STEG-3.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_4', 'AP_4', 'STEG-3.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_5', 'AP_5', 'STEG-4.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_6', 'AP_6', 'STEG-4.UT', 'N', '-', '-')");

        em.flush();
    }

    private void sql(String sql) {
        em.createNativeQuery(sql).executeUpdate();
    }


    private BehandlingModellImpl byggModell() {
        // Arrange - noen utvalge, tilfeldige aksjonspunkter
        AksjonspunktDefinisjon a0_0 = TestAksjonspunktDefinisjon.AP_1;
        AksjonspunktDefinisjon a0_1 = TestAksjonspunktDefinisjon.AP_2;
        AksjonspunktDefinisjon a1_0 = TestAksjonspunktDefinisjon.AP_3;
        AksjonspunktDefinisjon a1_1 = TestAksjonspunktDefinisjon.AP_4;
        AksjonspunktDefinisjon a2_0 = TestAksjonspunktDefinisjon.AP_5;
        AksjonspunktDefinisjon a2_1 = TestAksjonspunktDefinisjon.AP_6;

        DummySteg steg = new DummySteg();
        DummySteg steg0 = new DummySteg(opprettForAksjonspunkt(a2_0));
        DummySteg steg1 = new DummySteg();
        DummySteg steg2 = new DummySteg();

        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, steg, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, steg0, ap(a0_0), ap(a0_1)),
            new TestStegKonfig(STEG_3, behandlingType, steg1, ap(a1_0), ap(a1_1)),
            new TestStegKonfig(STEG_4, behandlingType, steg2, ap(a2_0), ap(a2_1))
        );

        return ModifiserbarBehandlingModell.setupModell(behandlingType, modellData);
    }

    private List<AksjonspunktDefinisjon> ap(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        return Arrays.asList(aksjonspunktDefinisjoner);
    }
}
