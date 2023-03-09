package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegUtfall;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.StegProsesseringResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;

@ExtendWith(JpaExtension.class)
class BehandlingModellTest {

    private static final LocalDateTime FRIST_TID = LocalDateTime.now().plusWeeks(4);

    private final BehandlingType behandlingType = BehandlingType.TILBAKEKREVING;

    private static final BehandlingStegType STEG_1 = BehandlingStegType.TBKGSTEG;
    private static final BehandlingStegType STEG_2 = BehandlingStegType.FAKTA_VERGE;
    private static final BehandlingStegType STEG_3 = BehandlingStegType.FAKTA_FEILUTBETALING;
    private static final BehandlingStegType STEG_4 = BehandlingStegType.FORELDELSEVURDERINGSTEG;

    private BehandlingskontrollTjeneste kontrollTjeneste;

    private BehandlingskontrollServiceProvider serviceProvider;
    private BehandlingRepositoryProvider repositoryProvider;


    private final DummySteg nullSteg = new DummySteg();
    private final DummyVenterSteg nullVenterSteg = new DummyVenterSteg();
    private final DummySteg aksjonspunktSteg = new DummySteg(
            opprettForAksjonspunkt(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
    private final DummySteg aksjonspunktModifisererSteg = new DummySteg(
            AksjonspunktResultat.opprettForAksjonspunktMedFrist(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, Venteårsak.AVVENTER_DOKUMENTASJON, FRIST_TID));

    @BeforeEach
    void setup(EntityManager em) {
        serviceProvider = new BehandlingskontrollServiceProvider(em, new BehandlingModellRepository(), null);
        repositoryProvider = new BehandlingRepositoryProvider(em);
        kontrollTjeneste = new BehandlingskontrollTjeneste(serviceProvider);
    }

    @Test
    void skal_finne_aksjonspunkter_som_ligger_etter_et_gitt_steg() {
        // Arrange - noen utvalge, tilfeldige aksjonspunkter
        var a0_0 = AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING;
        var a0_1 = AksjonspunktDefinisjon.AVKLAR_VERGE;
        var a1_0 = AksjonspunktDefinisjon.VURDER_FORELDELSE;
        var a1_1 = AksjonspunktDefinisjon.VURDER_TILBAKEKREVING;
        var a2_0 = AksjonspunktDefinisjon.FORESLÅ_VEDTAK;

        var steg = new DummySteg();
        var steg0 = new DummySteg();
        var steg1 = new DummySteg();
        var steg2 = new DummySteg();

        var modellData = List.of(
                new TestStegKonfig(STEG_1, behandlingType, steg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, steg0, ap(a0_0), ap(a0_1)),
                new TestStegKonfig(STEG_3, behandlingType, steg1, ap(a1_0), ap(a1_1)),
                new TestStegKonfig(STEG_4, behandlingType, steg2, ap(a2_0), ap()));

        var modell = setupModell(modellData);

        Set<AksjonspunktDefinisjon> ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_1);

        assertThat(ads).

                containsOnly(a0_0, a0_1, a1_0, a1_1, a2_0);

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_2);

        assertThat(ads).

                containsOnly(a1_0, a1_1, a2_0);

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_3);

        assertThat(ads).

                containsOnly(a2_0);

        ads = modell.finnAksjonspunktDefinisjonerEtter(STEG_4);

        assertThat(ads).

                isEmpty();

    }

    void skal_finne_aksjonspunkter_ved_inngang_eller_utgang_av_steg() {
        // Arrange - noen utvalge, tilfeldige aksjonspunkter
        var a0_0 = AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING;
        var a0_1 = AksjonspunktDefinisjon.AVKLAR_VERGE;
        var a1_0 = AksjonspunktDefinisjon.VURDER_FORELDELSE;
        var a1_1 = AksjonspunktDefinisjon.VURDER_TILBAKEKREVING;

        var steg = new DummySteg();
        var steg0 = new DummySteg();
        var steg1 = new DummySteg();

        var modellData = List.of(
                new TestStegKonfig(STEG_1, behandlingType, steg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, steg0, ap(a0_0), ap(a0_1)),
                new TestStegKonfig(STEG_3, behandlingType, steg1, ap(a1_0), ap(a1_1)));

        var modell = setupModell(modellData);

        Set<AksjonspunktDefinisjon> ads = modell.finnAksjonspunktDefinisjonerInngang(STEG_1);
        assertThat(ads).isEmpty();

        ads = modell.finnAksjonspunktDefinisjonerInngang(STEG_2);
        assertThat(ads).containsOnly(a0_0);

        ads = modell.finnAksjonspunktDefinisjonerUtgang(STEG_3);
        assertThat(ads).containsOnly(a1_1);

    }

    @Test
    void skal_stoppe_på_steg_2_når_får_aksjonspunkt() throws Exception {
        // Arrange
        var modellData = List.of(
                new TestStegKonfig(STEG_1, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, aksjonspunktSteg, ap(), ap()),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg,
                        ap(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING), ap()),
                new TestStegKonfig(STEG_4, behandlingType, nullSteg, ap(), ap()));
        var modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        var visitor = lagVisitor(behandling);
        var siste = modell.prosesserFra(STEG_1, visitor);

        assertThat(siste.behandlingStegType()).isEqualTo(STEG_3);
        assertThat(visitor.kjørteSteg).isEqualTo(List.of(STEG_1, STEG_2, STEG_3));
    }

    List<AksjonspunktDefinisjon> ap(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        return Arrays.asList(aksjonspunktDefinisjoner);
    }

    @Test
    void skal_kjøre_til_siste_når_ingen_gir_aksjonspunkt() {
        // Arrange
        List<TestStegKonfig> modellData = List.of(
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
        Assertions.assertThat(visitor.kjørteSteg).isEqualTo(List.of(STEG_1, STEG_2, STEG_3));
    }

    @Test
    void skal_stoppe_når_settes_på_vent_deretter_fortsette() {
        // Arrange
        List<TestStegKonfig> modellData = List.of(
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
        assertThat(første.behandlingStegType()).isEqualTo(STEG_2);
        assertThat(første.resultat()).isEqualTo(BehandlingStegStatus.VENTER);
        assertThat(visitor.kjørteSteg).isEqualTo(List.of(STEG_1, STEG_2));

        // Act 2
        BehandlingStegVisitorUtenLagring visitorNeste = lagVisitor(behandling);
        BehandlingStegUtfall neste = modell.prosesserFra(STEG_2, visitorNeste);

        assertThat(neste).isNotNull();
        assertThat(neste.behandlingStegType()).isEqualTo(STEG_2);
        assertThat(neste.resultat()).isEqualTo(BehandlingStegStatus.VENTER);
        assertThat(visitorNeste.kjørteSteg).isEqualTo(List.of(STEG_2));

        // Act 3
        BehandlingStegVisitorVenterUtenLagring gjenoppta = lagVisitorVenter(behandling);

        BehandlingStegUtfall fortsett = modell.prosesserFra(STEG_2, gjenoppta);
        assertThat(fortsett).isNull();
        assertThat(gjenoppta.kjørteSteg).isEqualTo(List.of(STEG_2, STEG_3));
    }

    @Test
    void skal_feile_ved_gjenopptak_vanlig_steg() {
        // Arrange
        List<TestStegKonfig> modellData = List.of(
                new TestStegKonfig(STEG_1, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg, ap(), ap()));
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act 1
        BehandlingStegVisitorVenterUtenLagring visitor = lagVisitorVenter(behandling);
        assertThrows(IllegalStateException.class, () -> modell.prosesserFra(STEG_1, visitor));
    }

    @Test
    void tilbakefører_til_tidligste_steg_med_åpent_aksjonspunkt() {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.AVKLAR_VERGE;
        DummySteg tilbakeføringssteg = new DummySteg(true, opprettForAksjonspunkt(aksjonspunktDefinisjon));
        // Arrange
        List<TestStegKonfig> modellData = List.of(
                new TestStegKonfig(STEG_1, behandlingType, nullSteg, ap(aksjonspunktDefinisjon), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_3, behandlingType, tilbakeføringssteg, ap(), ap()),
                new TestStegKonfig(STEG_4, behandlingType, nullSteg, ap(), ap()));
        BehandlingModellImpl modell = setupModell(modellData);

        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(behandling);

        var aksjonspunkt = serviceProvider.getAksjonspunktKontrollRepository()
                .leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon, STEG_1);
        serviceProvider.getAksjonspunktKontrollRepository().setReåpnet(aksjonspunkt);

        BehandlingStegUtfall siste = modell.prosesserFra(STEG_3, visitor);
        assertThat(siste.behandlingStegType()).isEqualTo(STEG_3);
        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_2);
    }

    @Test
    void finner_tidligste_steg_for_aksjonspunkter() {
        var aksjonspunktDefinisjon = STEG_2.getAksjonspunktDefinisjonerUtgang().get(0);
        List<TestStegKonfig> modellData = List.of(
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(aksjonspunktDefinisjon), ap()),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg, ap(), ap())
        );

        BehandlingModellImpl modell = setupModell(modellData);
        BehandlingStegModell behandlingStegModell = modell.finnTidligsteStegFor(Set.of(aksjonspunktDefinisjon));
        assertThat(behandlingStegModell.getBehandlingStegType()).isEqualTo(STEG_2);
    }

    @Test
    void skal_modifisere_aksjonspunktet_ved_å_kalle_funksjon_som_legger_til_frist() throws Exception {
        // Arrange
        List<TestStegKonfig> modellData = List.of(
                new TestStegKonfig(STEG_1, behandlingType, aksjonspunktModifisererSteg, ap(), ap()),
                new TestStegKonfig(STEG_2, behandlingType, nullSteg, ap(), ap()),
                new TestStegKonfig(STEG_3, behandlingType, nullSteg, ap(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING), ap())
        );
        BehandlingModellImpl modell = setupModell(modellData);
        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingStegVisitorUtenLagring visitor = lagVisitor(behandling);

        // Act
        modell.prosesserFra(STEG_1, visitor);

        // Assert
        var beh = hentBehandling(behandling.getId());
        assertThat(beh.getÅpneAksjonspunkter()).hasSize(1);
        assertThat(beh.getÅpneAksjonspunkter().get(0).getFristTid()).isEqualTo(FRIST_TID);
    }

    private Behandling hentBehandling(Long behandlingId) {
        return repositoryProvider.getBehandlingRepository().hentBehandling(behandlingId);
    }

    private BehandlingModellImpl setupModell(List<TestStegKonfig> resolve) {
        return ModifiserbarBehandlingModell.setupModell(behandlingType, resolve);
    }

    private BehandlingStegVisitorUtenLagring lagVisitor(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling);
        return new BehandlingStegVisitorUtenLagring(serviceProvider, kontekst);
    }

    private BehandlingStegVisitorVenterUtenLagring lagVisitorVenter(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = kontrollTjeneste.initBehandlingskontroll(behandling);
        return new BehandlingStegVisitorVenterUtenLagring(serviceProvider, kontekst);
    }

    static class BehandlingStegVisitorUtenLagring extends TekniskBehandlingStegVisitor {
        List<BehandlingStegType> kjørteSteg = new ArrayList<>();

        BehandlingStegVisitorUtenLagring(BehandlingskontrollServiceProvider serviceProvider,
                                         BehandlingskontrollKontekst kontekst) {
            super(serviceProvider, kontekst);
        }

        @Override
        public StegProsesseringResultat prosesserStegISavepoint(Behandling behandling, BehandlingStegVisitor stegVisitor) {
            // bypass savepoint
            this.kjørteSteg.add(stegVisitor.getStegModell().getBehandlingStegType());
            return super.prosesserSteg(stegVisitor);
        }
    }

    static class BehandlingStegVisitorVenterUtenLagring extends TekniskBehandlingStegVenterVisitor {
        List<BehandlingStegType> kjørteSteg = new ArrayList<>();

        public BehandlingStegVisitorVenterUtenLagring(BehandlingskontrollServiceProvider serviceProvider,
                                               BehandlingskontrollKontekst kontekst) {
            super(serviceProvider, kontekst);
        }

        @Override
        protected StegProsesseringResultat prosesserStegISavepoint(Behandling behandling, BehandlingStegVisitor stegVisitor) {
            // bypass savepoint
            this.kjørteSteg.add(stegVisitor.getStegModell().getBehandlingStegType());
            return super.prosesserSteg(stegVisitor);
        }
    }
}
