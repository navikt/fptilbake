package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModellVisitor;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegUtfall;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellImpl.TriFunction;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BehandlingskontrollTjenesteTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private EntityManager em = repoRule.getEntityManager();


    private final class BehandlingskontrollEventPublisererForTest extends BehandlingskontrollEventPubliserer {
        private List<BehandlingEvent> events = new ArrayList<>();

        @Override
        protected void doFireEvent(BehandlingEvent event) {
            events.add(event);
        }
    }

    static class BehandlingModellForTest {
        BehandlingType behandlingType = BehandlingType.TILBAKEKREVING;

        // random liste av aksjonspunkt og steg i en definert rekkefølge for å kunne sette opp modellen
        AksjonspunktDefinisjon a2_0 = TestAksjonspunktDefinisjon.AP_1;
        AksjonspunktDefinisjon a2_1 = TestAksjonspunktDefinisjon.AP_2;
        AksjonspunktDefinisjon a3_0 = TestAksjonspunktDefinisjon.AP_3;
        AksjonspunktDefinisjon a3_1 = TestAksjonspunktDefinisjon.AP_4;
        AksjonspunktDefinisjon a4_0 = TestAksjonspunktDefinisjon.AP_5;
        AksjonspunktDefinisjon a4_1 = TestAksjonspunktDefinisjon.AP_6;
        AksjonspunktDefinisjon a5_0 = TestAksjonspunktDefinisjon.AP_7;
        AksjonspunktDefinisjon a5_1 = TestAksjonspunktDefinisjon.AP_8;

        DummySteg steg1 = new DummySteg();
        DummySteg steg2 = new DummySteg();
        DummySteg steg3 = new DummySteg();
        DummySteg steg4 = new DummySteg();
        DummySteg steg5 = new DummySteg();

        List<TestStegKonfig> modellData = Arrays.asList(
            new TestStegKonfig(STEG_1, behandlingType, steg1, ap(), ap()),
            new TestStegKonfig(STEG_2, behandlingType, steg2, ap(a2_0), ap(a2_1)),
            new TestStegKonfig(STEG_3, behandlingType, steg3, ap(a3_0), ap(a3_1)),
            new TestStegKonfig(STEG_4, behandlingType, steg4, ap(a4_0), ap(a4_1)),
            new TestStegKonfig(STEG_5, behandlingType, steg5, ap(a5_0), ap(a5_1))
        );

        BehandlingModellImpl modell = setupModell(behandlingType, modellData);
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private BehandlingModellForTest behandlingModellForTest = new BehandlingModellForTest();

    private static final BehandlingStegType STEG_1 = TestBehandlingStegType.STEG_1;
    private static final BehandlingStegType STEG_2 = TestBehandlingStegType.STEG_2;
    private static final BehandlingStegType STEG_3 = TestBehandlingStegType.STEG_3;
    private static final BehandlingStegType STEG_4 = TestBehandlingStegType.STEG_4;
    private static final BehandlingStegType STEG_5 = TestBehandlingStegType.STEG_5;

    private BehandlingskontrollTjeneste kontrollTjeneste;

    private Behandling behandling;

    private BehandlingskontrollKontekst kontekst;

    private BehandlingskontrollEventPublisererForTest eventPubliserer = new BehandlingskontrollEventPublisererForTest();

    @Inject
    private InternalManipulerBehandling manipulerInternBehandling;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Before
    public void setup() {
        opprettStatiskModell();

        ScenarioSimple scenario = ScenarioSimple.simple()
            .medBehandlingType(BehandlingType.TILBAKEKREVING);
        behandling = scenario.lagre(repositoryProvider);

        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, STEG_3);

        initBehandlingskontrollTjeneste(this.behandlingModellForTest.modell);

        kontekst = Mockito.mock(BehandlingskontrollKontekst.class);
        Mockito.when(kontekst.getBehandlingId()).thenReturn(behandling.getId());
        Mockito.when(kontekst.getFagsakId()).thenReturn(behandling.getFagsakId());
    }

    @Test
    public void skal_rykke_tilbake_til_inngang_vurderingspunkt_av_steg() {

        String steg2InngangAksjonspunkt = this.behandlingModellForTest.a2_0.getKode();

        kontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, Arrays.asList(steg2InngangAksjonspunkt), false);

        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_2);
        Assertions.assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        Assertions.assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        Assertions.assertThat(behandling.getBehandlingStegTilstand()).isNotNull();
        Assertions.assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(behandling, STEG_3,
            BehandlingStegStatus.TILBAKEFØRT);
        sjekkBehandlingStegTilstandHistorikk(behandling, STEG_2,
            BehandlingStegStatus.INNGANG);

    }

    @Test
    public void skal_rykke_tilbake_til_utgang_vurderingspunkt_av_steg() {

        String steg2UtgangAksjonspunkt = this.behandlingModellForTest.a2_1.getKode();

        kontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, Arrays.asList(steg2UtgangAksjonspunkt), false);

        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_2);
        Assertions.assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        Assertions.assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.UTGANG);
        Assertions.assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        Assertions.assertThat(behandling.getBehandlingStegTilstand(STEG_2)).isPresent();
        Assertions.assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(behandling, STEG_3,
            BehandlingStegStatus.TILBAKEFØRT);
        sjekkBehandlingStegTilstandHistorikk(behandling, STEG_2,
            BehandlingStegStatus.UTGANG);

    }

    @Test
    public void skal_rykke_tilbake_til_start_av_tidligere_steg_ved_tilbakeføring() {

        kontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, STEG_2);

        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_2);
        Assertions.assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        Assertions.assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.UTGANG);
        Assertions.assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        Assertions.assertThat(behandling.getBehandlingStegTilstand(STEG_2)).isPresent();
        Assertions.assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(behandling, STEG_3,
            BehandlingStegStatus.TILBAKEFØRT);
        sjekkBehandlingStegTilstandHistorikk(behandling, STEG_2,
            BehandlingStegStatus.UTGANG);

    }

    @Test
    public void skal_tolerere_tilbakehopp_til_senere_steg_enn_inneværende() {

        kontrollTjeneste.behandlingTilbakeføringHvisTidligereBehandlingSteg(kontekst, STEG_4);

        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_3);
        Assertions.assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        Assertions.assertThat(behandling.getBehandlingStegStatus()).isNull();
        Assertions.assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        Assertions.assertThat(behandling.getBehandlingStegTilstand(STEG_3)).isPresent();
        Assertions.assertThat(behandling.getBehandlingStegTilstand(STEG_4)).isNotPresent();
        Assertions.assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(1);
    }

    @Test
    public void skal_flytte_til__inngang_av_senere_steg_ved_framføring() {

        kontrollTjeneste.behandlingFramføringTilSenereBehandlingSteg(kontekst, STEG_5);

        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_5);
        Assertions.assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        Assertions.assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        Assertions.assertThat(behandling.getBehandlingStegTilstand(STEG_5)).isPresent();
        Assertions.assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(behandling, STEG_3,
            BehandlingStegStatus.AVBRUTT);

        // NB: skipper STEP_4
        sjekkBehandlingStegTilstandHistorikk(behandling, STEG_4);

        sjekkBehandlingStegTilstandHistorikk(behandling, STEG_5,
            BehandlingStegStatus.INNGANG);

    }

    @Test(expected = IllegalStateException.class)
    public void skal_kaste_exception_dersom_ugyldig_tilbakeføring() {
        kontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, STEG_4);

    }

    @Test
    public void skal_rykke_tilbake_til_inngang_vurderingspunkt_av_samme_steg() {

        // Arrange
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, STEG_4, BehandlingStegStatus.UTGANG,
            BehandlingStegStatus.AVBRUTT);

        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_4);
        Assertions.assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.UTGANG);

        String steg4InngangAksjonspunkt = this.behandlingModellForTest.a4_0.getKode();

        // Act
        kontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, Arrays.asList(steg4InngangAksjonspunkt), false);

        // Assert
        Assertions.assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(STEG_4);
        Assertions.assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        Assertions.assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        Assertions.assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(3);

        sjekkBehandlingStegTilstandHistorikk(
            behandling, STEG_4, BehandlingStegStatus.TILBAKEFØRT, BehandlingStegStatus.INNGANG);

        Assertions.assertThat(behandling.getBehandlingStegTilstand(STEG_4).get().getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);

    }

    @Test
    public void skal_ha_guard_mot_nøstet_behandlingskontroll_ved_prossesering_tilbakeføring_og_framføring() throws Exception {

        BehandlingModellRepository behandlingModellRepository = Mockito.mock(BehandlingModellRepository.class);
        Mockito.when(behandlingModellRepository.getModell(Mockito.any())).thenReturn(this.behandlingModellForTest.modell);
        this.kontrollTjeneste = new BehandlingskontrollTjeneste(repositoryProvider, behandlingModellRepository,
            eventPubliserer) {
            @Override
            protected BehandlingStegUtfall doProsesserBehandling(BehandlingskontrollKontekst kontekst, BehandlingModell modell, BehandlingModellVisitor visitor) {
                kontrollTjeneste.prosesserBehandling(kontekst);
                return null;
            }
        };

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Støtter ikke nøstet prosessering");

        this.kontrollTjeneste.prosesserBehandling(kontekst);
    }

    @Test
    public void skal_returnere_true_når_aksjonspunktet_skal_løses_etter_angitt_steg() {
        assertThat(kontrollTjeneste.skalAksjonspunktReaktiveresIEllerEtterSteg(behandling, STEG_4, TestAksjonspunktDefinisjon.AP_7)).isTrue();
    }

    @Test
    public void skal_returnere_true_når_aksjonspunktet_skal_løses_i_angitt_steg() {
        assertThat(kontrollTjeneste.skalAksjonspunktReaktiveresIEllerEtterSteg(behandling, STEG_4, TestAksjonspunktDefinisjon.AP_6)).isTrue();
    }

    @Test
    public void skal_returnere_false_når_aksjonspunktet_skal_løses_før_angitt_steg() {
        assertThat(kontrollTjeneste.skalAksjonspunktReaktiveresIEllerEtterSteg(behandling, STEG_4, TestAksjonspunktDefinisjon.AP_4)).isFalse();
    }

    private void sjekkBehandlingStegTilstandHistorikk(Behandling behandling, BehandlingStegType stegType,
                                                      BehandlingStegStatus... stegStatuser) {
        Assertions.assertThat(
            behandling.getBehandlingStegTilstandHistorikk()
                .filter(bst -> stegType == null || Objects.equals(bst.getBehandlingSteg(), stegType))
                .map(bst -> bst.getBehandlingStegStatus()))
            .containsExactly(stegStatuser);
    }

    private static List<AksjonspunktDefinisjon> ap(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        return Arrays.asList(aksjonspunktDefinisjoner);
    }

    private static BehandlingModellImpl setupModell(BehandlingType behandlingType, List<TestStegKonfig> resolve) {
        TriFunction<BehandlingStegType, BehandlingType, BehandlingSteg> finnSteg = DummySteg.map(resolve);

        BehandlingModellImpl modell = new BehandlingModellImpl(behandlingType, finnSteg);
        for (TestStegKonfig konfig : resolve) {
            BehandlingStegType stegType = konfig.getBehandlingStegType();

            // fake legg til behandlingSteg og vureringspunkter
            ModifiserbarBehandlingModell.ModifiserbarBehandlingStegType modStegType = ModifiserbarBehandlingModell.fra(stegType);
            modell.leggTil(modStegType, behandlingType);

            ModifiserbarBehandlingModell.ModifiserbarVurderingspunktDefinisjon modVurderingspunktInngang = ModifiserbarBehandlingModell.fra(modStegType,
                VurderingspunktDefinisjon.Type.INNGANG);
            modStegType.leggTilVurderingspunkt(modVurderingspunktInngang);
            modVurderingspunktInngang.leggTil(konfig.getInngangAksjonspunkter());

            ModifiserbarBehandlingModell.ModifiserbarVurderingspunktDefinisjon modVurderingspunktUtgang = ModifiserbarBehandlingModell.fra(modStegType,
                VurderingspunktDefinisjon.Type.UTGANG);
            modStegType.leggTilVurderingspunkt(modVurderingspunktUtgang);
            modVurderingspunktUtgang.leggTil(konfig.getUtgangAksjonspunkter());

            modell.internFinnSteg(stegType).leggTilVurderingspunktInngang(Optional.of(modVurderingspunktInngang));
            modell.internFinnSteg(stegType).leggTilVurderingspunktUtgang(Optional.of(modVurderingspunktUtgang));

        }
        return modell;

    }

    private void initBehandlingskontrollTjeneste(BehandlingModellImpl modell) {
        BehandlingModellRepository behandlingModellRepository = Mockito.mock(BehandlingModellRepository.class);
        Mockito.when(behandlingModellRepository.getModell(Mockito.any())).thenReturn(modell);
        Mockito.when(behandlingModellRepository.getBehandlingStegKonfigurasjon()).thenReturn(BehandlingStegKonfigurasjon.lagDummy());
        this.kontrollTjeneste = new BehandlingskontrollTjeneste(repositoryProvider, behandlingModellRepository, eventPubliserer);
    }

    private void opprettStatiskModell() {
        sql("INSERT INTO KODELISTE (id, kodeverk, kode, ekstra_data) values (seq_kodeliste.nextval, 'BEHANDLING_TYPE', 'BT-TEST', '{behandlingstidFristUker: 3}')");

        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-1', 'test-steg-1', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-2', 'test-steg-2', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-3', 'test-steg-3', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-4', 'test-steg-4', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-5', 'test-steg-5', 'UTRED', 'test')");

        em.flush();
    }

    private void sql(String sql) {
        em.createNativeQuery(sql).executeUpdate();
    }


}
