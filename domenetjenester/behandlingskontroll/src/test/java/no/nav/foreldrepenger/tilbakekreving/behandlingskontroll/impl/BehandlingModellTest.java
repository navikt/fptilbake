package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;
import static no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunktMedCallback;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegProsesseringResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegUtfall;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@SuppressWarnings("resource")
@RunWith(CdiRunner.class)
public class BehandlingModellTest {

    private static final LocalDateTime FRIST_TID = LocalDateTime.now().plusWeeks(4);

    private final BehandlingType behandlingType = BehandlingType.TILBAKEKREVING;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private EntityManager em = repoRule.getEntityManager();

    private static final BehandlingStegType STEG_1 = TestBehandlingStegType.STEG_1;
    private static final BehandlingStegType STEG_2 = TestBehandlingStegType.STEG_2;
    private static final BehandlingStegType STEG_3 = TestBehandlingStegType.STEG_3;
    private static final BehandlingStegType STEG_4 = TestBehandlingStegType.STEG_4;

    @Inject
    private BehandlingskontrollTjeneste kontrollTjeneste;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private AksjonspunktRepository aksjonspunktRepository;

    private final DummySteg nullSteg = new DummySteg();
    private final DummyVenterSteg nullVenterSteg = new DummyVenterSteg();
    private final DummySteg aksjonspunktSteg = new DummySteg(opprettForAksjonspunkt(TestAksjonspunktDefinisjon.AP_1));
    private final DummySteg aksjonspunktModifisererSteg = new DummySteg(opprettForAksjonspunktMedCallback(
            TestAksjonspunktDefinisjon.AP_1, (ap) -> {
                aksjonspunktRepository.setFrist(ap, FRIST_TID, Venteårsak.UDEFINERT);
            }));

    @Test
    public void skal_finne_aksjonspunkter_som_ligger_etter_et_gitt_steg() {
        // Arrange - noen utvalge, tilfeldige aksjonspunkter
        AksjonspunktDefinisjon a0_0 = TestAksjonspunktDefinisjon.AP_1;
        AksjonspunktDefinisjon a0_1 = TestAksjonspunktDefinisjon.AP_2;
        AksjonspunktDefinisjon a1_0 = TestAksjonspunktDefinisjon.AP_3;
        AksjonspunktDefinisjon a1_1 = TestAksjonspunktDefinisjon.AP_4;
        AksjonspunktDefinisjon a2_0 = TestAksjonspunktDefinisjon.AP_5;
        AksjonspunktDefinisjon a2_1 = TestAksjonspunktDefinisjon.AP_6;

        DummySteg steg = new DummySteg();
        DummySteg steg0 = new DummySteg();
        DummySteg steg1 = new DummySteg();
        DummySteg steg2 = new DummySteg();

        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, steg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, steg0, ap(a0_0), ap(a0_1)),
                new TestStegKonfig(STEG_3, behandlingType, steg1, ap(a1_0), ap(a1_1)),
                new TestStegKonfig(STEG_4, behandlingType, steg2, ap(a2_0), ap(a2_1)));

        BehandlingModellImpl modell = setupModell(modellData);

        Set<String> ads = null;

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_1);

        Assertions.assertThat(ads).

                containsOnly(a0_0.getKode(), a0_1.

                        getKode(), a1_0.

                        getKode(), a1_1.

                        getKode(), a2_0.

                        getKode(), a2_1.

                        getKode());

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_2);

        Assertions.assertThat(ads).

                containsOnly(a1_0.getKode(), a1_1.

                        getKode(), a2_0.

                        getKode(), a2_1.

                        getKode());

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_3);

        Assertions.assertThat(ads).

                containsOnly(a2_0.getKode(), a2_1.

                        getKode());

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_4);

        Assertions.assertThat(ads).

                isEmpty();

    }

    @Test
    public void skal_finne_aksjonspunkter_ved_inngang_eller_utgang_av_steg() {
        // Arrange - noen utvalge, tilfeldige aksjonspunkter
        AksjonspunktDefinisjon a0_0 = TestAksjonspunktDefinisjon.AP_1;
        AksjonspunktDefinisjon a0_1 = TestAksjonspunktDefinisjon.AP_2;
        AksjonspunktDefinisjon a1_0 = TestAksjonspunktDefinisjon.AP_3;
        AksjonspunktDefinisjon a1_1 = TestAksjonspunktDefinisjon.AP_4;

        DummySteg steg = new DummySteg();
        DummySteg steg0 = new DummySteg();
        DummySteg steg1 = new DummySteg();

        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, steg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, steg0, ap(a0_0), ap(a0_1)),
                new TestStegKonfig(STEG_3, behandlingType, steg1, ap(a1_0), ap(a1_1))
        );

        BehandlingModellImpl modell = setupModell(modellData);

        Set<String> ads = null;

        ads = modell.finnAksjonspunktDefinisjonerInngang(STEG_1);
        Assertions.assertThat(ads).isEmpty();

        ads = modell.finnAksjonspunktDefinisjonerInngang(STEG_2);
        Assertions.assertThat(ads).containsOnly(a0_0.getKode());

        ads = modell.finnAksjonspunktDefinisjonerUtgang(STEG_3);
        Assertions.assertThat(ads).containsOnly(a1_1.getKode());

    }

    @Test
    public void skal_stoppe_på_steg_2_når_får_aksjonspunkt() throws Exception {
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, aksjonspunktSteg, ap(), ap(TestAksjonspunktDefinisjon.AP_1)),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg, ap(), ap())
        );
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(behandling);
        BehandlingStegUtfall siste = modell.prosesserFra(STEG_1, visitor);

        assertThat(siste.getBehandlingStegType()).isEqualTo(STEG_2);
        Assertions.assertThat(visitor.kjørteSteg).isEqualTo(Arrays.asList(STEG_1, STEG_2));
    }

    public List<AksjonspunktDefinisjon> ap(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        return Arrays.asList(aksjonspunktDefinisjoner);
    }

    @Test
    public void skal_kjøre_til_siste_når_ingen_gir_aksjonspunkt() {
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg, ap(), ap())
        );
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(behandling);
        BehandlingStegUtfall siste = modell.prosesserFra(STEG_1, visitor);

        assertThat(siste).isNull();
        Assertions.assertThat(visitor.kjørteSteg).isEqualTo(Arrays.asList(STEG_1, STEG_2, STEG_3));
    }

    @Test
    public void skal_stoppe_når_settes_på_vent_deretter_fortsette() {
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullVenterSteg, ap(), ap()),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg, ap(), ap())
        );
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act 1
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(behandling);

        BehandlingStegUtfall første = modell.prosesserFra(STEG_1, visitor);

        assertThat(første).isNotNull();
        assertThat(første.getBehandlingStegType()).isEqualTo(STEG_2);
        assertThat(første.getResultat()).isEqualTo(BehandlingStegStatus.VENTER);
        assertThat(visitor.kjørteSteg).isEqualTo(Arrays.asList(STEG_1, STEG_2));

        // Act 2
        BehandlingStegVisitorUtenLagring visitorNeste = lagVisitor(behandling);
        BehandlingStegUtfall neste = modell.prosesserFra(STEG_2, visitorNeste);

        assertThat(neste).isNotNull();
        assertThat(neste.getBehandlingStegType()).isEqualTo(STEG_2);
        assertThat(neste.getResultat()).isEqualTo(BehandlingStegStatus.VENTER);
        assertThat(visitorNeste.kjørteSteg).isEqualTo(Arrays.asList(STEG_2));

        // Act 3
        BehandlingStegVisitorVenterUtenLagring gjenoppta = lagVisitorVenter(behandling);

        BehandlingStegUtfall fortsett = modell.prosesserFra(STEG_2, gjenoppta);
        assertThat(fortsett).isNull();
        assertThat(gjenoppta.kjørteSteg).isEqualTo(Arrays.asList(STEG_2, STEG_3));
    }

    @Test(expected = IllegalStateException.class)
    public void skal_feile_ved_gjenopptak_vanlig_steg() {
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg, ap(), ap()));
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act 1
        BehandlingStegVisitorVenterUtenLagring visitor = lagVisitorVenter(behandling);
        modell.prosesserFra(STEG_1, visitor);
    }

    @Test
    public void tilbakefører_til_tidligste_steg_med_åpent_aksjonspunkt() {
        AksjonspunktDefinisjon avklarFødsel = TestAksjonspunktDefinisjon.AP_1;
        DummySteg tilbakeføringssteg = new DummySteg(true, opprettForAksjonspunkt(avklarFødsel));
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, nullSteg, ap(avklarFødsel), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_3, behandlingType, tilbakeføringssteg, ap(), ap())
        );
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(behandling);

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, TestAksjonspunktDefinisjon.AP_1,
                STEG_1);
        aksjonspunktRepository.setReåpnet(aksjonspunkt);

        BehandlingStegUtfall siste = modell.prosesserFra(STEG_3, visitor);
        assertThat(siste.getBehandlingStegType()).isEqualTo(STEG_3);
        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_1);
    }

    @Test
    public void finner_tidligste_steg_for_aksjonspunkter() {
        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, nullSteg, ap(TestAksjonspunktDefinisjon.AP_1), ap()),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg, ap(), ap())
        );

        BehandlingModellImpl modell = setupModell(modellData);
        Set<AksjonspunktDefinisjon> aksjonspunktDefinisjoner = new HashSet<>();
        aksjonspunktDefinisjoner.add(TestAksjonspunktDefinisjon.AP_1);
        BehandlingStegModell behandlingStegModell = modell.finnTidligsteStegFor(aksjonspunktDefinisjoner);
        assertThat(behandlingStegModell.getBehandlingStegType()).isEqualTo(STEG_1);
    }

    @Test
    public void skal_modifisere_aksjonspunktet_ved_å_kalle_funksjon_som_legger_til_frist() throws Exception {
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, aksjonspunktModifisererSteg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(), ap(TestAksjonspunktDefinisjon.AP_1)),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg, ap(), ap())
        );
        BehandlingModellImpl modell = setupModell(modellData);
        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(behandling);

        // Act
        modell.prosesserFra(STEG_1, visitor);

        // Assert
        Behandling beh = hentBehandling(behandling.getId());
        assertThat(beh.getÅpneAksjonspunkter().size()).isEqualTo(1);
        assertThat(beh.getÅpneAksjonspunkter().get(0).getFristTid()).isEqualTo(FRIST_TID);
    }

    private Behandling hentBehandling(Long behandlingId) {
        return repositoryProvider.getBehandlingRepository().hentBehandling(behandlingId);
    }

    @Test
    public void skal_reaktiveree_aksjonspunkt_som_steget_har_som_resultat() throws Exception {
        AksjonspunktDefinisjon apd = TestAksjonspunktDefinisjon.AP_1;
        DummySteg stegSomOpretterAksjonspunkt = new DummySteg(true, opprettForAksjonspunkt(apd));
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, stegSomOpretterAksjonspunkt, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(), ap(apd)),
                new TestStegKonfig(STEG_4, behandlingType, nullSteg, ap(), ap())
        );
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(behandling);

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, apd, STEG_2);
        aksjonspunktRepository.setTilUtført(aksjonspunkt);
        aksjonspunktRepository.deaktiver(aksjonspunkt);

        BehandlingStegUtfall siste = modell.prosesserFra(STEG_1, visitor);
        assertThat(siste.getBehandlingStegType()).isEqualTo(STEG_1);
        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_2);

        Assertions.assertThat(behandling.getAksjonspunktFor(apd).erAktivt()).isTrue();
    }

    @Test
    public void skal_ikke_reaktivere_aksjonpunkt_som_ikke_er_fra_stegets_resultat() throws Exception {
        AksjonspunktDefinisjon apd = TestAksjonspunktDefinisjon.AP_1;
        AksjonspunktDefinisjon apd2 = TestAksjonspunktDefinisjon.AP_2;
        DummySteg stegSomOpretterAksjonspunkt = new DummySteg(true, opprettForAksjonspunkt(apd));
        // Arrange
        List<TestStegKonfig> modellData = Arrays.asList(
                new TestStegKonfig(STEG_1, behandlingType, stegSomOpretterAksjonspunkt, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(), ap(apd, apd2)),
                new TestStegKonfig(STEG_4, behandlingType, nullSteg, ap(), ap())
        );
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(behandling);

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, apd, STEG_2);
        aksjonspunktRepository.setTilUtført(aksjonspunkt);
        aksjonspunktRepository.deaktiver(aksjonspunkt);

        Aksjonspunkt aksjonspunkt2 = aksjonspunktRepository.leggTilAksjonspunkt(behandling, apd2, STEG_2);
        aksjonspunktRepository.setTilUtført(aksjonspunkt2);
        aksjonspunktRepository.deaktiver(aksjonspunkt2);

        BehandlingStegUtfall siste = modell.prosesserFra(STEG_1, visitor);
        assertThat(siste.getBehandlingStegType()).isEqualTo(STEG_1);
        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_2);

        Assertions.assertThat(behandling.getAksjonspunktFor(apd).erAktivt()).isTrue();
        Aksjonspunkt ap2 = behandling.getAlleAksjonspunkterInklInaktive().stream()
                .filter(a -> a.getAksjonspunktDefinisjon().equals(apd2))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Mangler aksjonspunkt med def " + apd2));
        Assertions.assertThat(ap2.erAktivt()).isFalse();
    }

    private BehandlingModellImpl setupModell(List<TestStegKonfig> resolve) {
        return ModifiserbarBehandlingModell.setupModell(behandlingType, resolve);
    }

    private BehandlingStegVisitorUtenLagring lagVisitor(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling);
        return new BehandlingStegVisitorUtenLagring(repositoryProvider, kontrollTjeneste, kontekst, BehandlingskontrollEventPubliserer.NULL_EVENT_PUB);
    }

    private BehandlingStegVisitorVenterUtenLagring lagVisitorVenter(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling);
        return new BehandlingStegVisitorVenterUtenLagring(repositoryProvider, kontrollTjeneste, kontekst, BehandlingskontrollEventPubliserer.NULL_EVENT_PUB);
    }

    @Before
    public void opprettStatiskModell() {
        sql("INSERT INTO KODELISTE (id, kodeverk, kode, ekstra_data) values (seq_kodeliste.nextval, 'BEHANDLING_TYPE', 'BT-TEST2', '{behandlingstidFristUker: 3}')");

        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-1', 'test-steg-1', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-2', 'test-steg-2', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-3', 'test-steg-3', 'UTRED', 'test')");

        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-1.INN', 'STEG-1', 'INN', 'STEG-1.INN')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-2.INN', 'STEG-2', 'INN', 'STEG-2.INN')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-3.INN', 'STEG-3', 'INN', 'STEG-3.INN')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-1.UT', 'STEG-1', 'UT', 'STEG-1.UT')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-2.UT', 'STEG-2', 'UT', 'STEG-2.UT')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-3.UT', 'STEG-3', 'UT', 'STEG-3.UT')");

        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_1', 'AP 1', 'STEG-1.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_2', 'AP_2', 'STEG-2.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_3', 'AP_3', 'STEG-3.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_4', 'AP_4', 'STEG-1.UT', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_5', 'AP_5', 'STEG-2.UT', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('AP_6', 'AP_6', 'STEG-3.UT', 'N', '-', '-')");

        em.flush();
    }

    private void sql(String sql) {
        em.createNativeQuery(sql).executeUpdate();
    }


    static class BehandlingStegVisitorUtenLagring extends TekniskBehandlingStegVisitor {
        List<BehandlingStegType> kjørteSteg = new ArrayList<>();

        BehandlingStegVisitorUtenLagring(BehandlingRepositoryProvider repositoryProvider,
                                         BehandlingskontrollTjeneste tjeneste,
                                         BehandlingskontrollKontekst kontekst,
                                         BehandlingskontrollEventPubliserer eventPubliserer) {
            super(repositoryProvider, tjeneste, kontekst, eventPubliserer);
        }

        @Override
        public BehandlingStegProsesseringResultat prosesserStegISavepoint(Behandling behandling, BehandlingStegVisitor stegVisitor) {
            // bypass savepoint
            this.kjørteSteg.add(stegVisitor.getStegModell().getBehandlingStegType());
            return super.prosesserSteg(stegVisitor);
        }
    }

    static class BehandlingStegVisitorVenterUtenLagring extends TekniskBehandlingStegVenterVisitor {
        List<BehandlingStegType> kjørteSteg = new ArrayList<>();

        BehandlingStegVisitorVenterUtenLagring(BehandlingRepositoryProvider repositoryProvider,
                                               BehandlingskontrollTjeneste tjeneste,
                                               BehandlingskontrollKontekst kontekst,
                                               BehandlingskontrollEventPubliserer eventPubliserer) {
            super(repositoryProvider, tjeneste, kontekst, eventPubliserer);
        }

        @Override
        protected BehandlingStegProsesseringResultat prosesserStegISavepoint(Behandling behandling, BehandlingStegVisitor stegVisitor) {
            // bypass savepoint
            this.kjørteSteg.add(stegVisitor.getStegModell().getBehandlingStegType());
            return super.prosesserSteg(stegVisitor);
        }
    }
}
