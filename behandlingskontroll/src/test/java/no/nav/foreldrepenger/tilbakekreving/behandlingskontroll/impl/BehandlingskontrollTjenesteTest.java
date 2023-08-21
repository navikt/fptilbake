package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling.forceOppdaterBehandlingSteg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModellVisitor;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegUtfall;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;

@ExtendWith(JpaExtension.class)
class BehandlingskontrollTjenesteTest {

    private final class BehandlingskontrollEventPublisererForTest extends BehandlingskontrollEventPubliserer {

        private List<BehandlingEvent> events = new ArrayList<>();

        @Override
        protected void doFireEvent(BehandlingEvent event) {
            events.add(event);
        }

    }

    private BehandlingskontrollTjeneste kontrollTjeneste;

    private Behandling behandling;

    private BehandlingskontrollKontekst kontekst;

    private final BehandlingskontrollEventPublisererForTest eventPubliserer = new BehandlingskontrollEventPublisererForTest();
    private BehandlingskontrollServiceProvider serviceProvider;
    private BehandlingRepositoryProvider repositoryProvider;


    private BehandlingStegType steg2;
    private BehandlingStegType steg3;
    private BehandlingStegType steg4;
    private BehandlingStegType steg5;

    private AksjonspunktDefinisjon steg2Aksjonspunkt;

    @BeforeEach
    void setup(EntityManager em) {
        serviceProvider = new BehandlingskontrollServiceProvider(em, new BehandlingModellRepository(), eventPubliserer);
        repositoryProvider = new BehandlingRepositoryProvider(em);
        var modell = serviceProvider.getBehandlingModellRepository().getModell(BehandlingType.TILBAKEKREVING);

        steg2 = modell.getAlleBehandlingStegTyper().get(3);
        steg3 = modell.finnNesteSteg(steg2).getBehandlingStegType();
        steg4 = modell.finnNesteSteg(steg3).getBehandlingStegType();
        steg5 = modell.finnNesteSteg(steg4).getBehandlingStegType();
        steg2Aksjonspunkt = modell.finnAksjonspunktDefinisjonerUtgang(steg2).iterator().next();

        ScenarioSimple scenario = ScenarioSimple.simple()
                .medBehandlingType(BehandlingType.TILBAKEKREVING);
        behandling = scenario.lagre(repositoryProvider);

        forceOppdaterBehandlingSteg(behandling, steg3);

        kontekst = mock(BehandlingskontrollKontekst.class);
        lenient().when(kontekst.getBehandlingId()).thenReturn(behandling.getId());
        lenient().when(kontekst.getFagsakId()).thenReturn(behandling.getFagsakId());

        this.kontrollTjeneste = new BehandlingskontrollTjeneste(serviceProvider);
    }

    @Test
    void skal_rykke_tilbake_til_utgang_vurderingspunkt_av_steg() {

        kontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, List.of(steg2Aksjonspunkt));

        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(steg2);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.UTGANG);
        assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        assertThat(behandling.getBehandlingStegTilstand(steg2)).isPresent();
        assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(behandling, steg3, BehandlingStegStatus.TILBAKEFØRT);
        sjekkBehandlingStegTilstandHistorikk(behandling, steg2, BehandlingStegStatus.UTGANG);

    }

    @Test
    void skal_rykke_tilbake_til_start_av_tidligere_steg_ved_tilbakeføring() {

        kontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, steg2);

        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(steg2);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        assertThat(behandling.getBehandlingStegTilstand(steg2)).isPresent();
        assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(behandling, steg3, BehandlingStegStatus.TILBAKEFØRT);
        sjekkBehandlingStegTilstandHistorikk(behandling, steg2, BehandlingStegStatus.INNGANG);

    }

    @Test
    void skal_tolerere_tilbakehopp_til_senere_steg_enn_inneværende() {

        kontrollTjeneste.behandlingTilbakeføringHvisTidligereBehandlingSteg(kontekst, steg4);

        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(steg3);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(behandling.getBehandlingStegStatus()).isNull();
        assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        assertThat(behandling.getBehandlingStegTilstand(steg3)).isPresent();
        assertThat(behandling.getBehandlingStegTilstand(steg4)).isNotPresent();
        assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(1);
    }

    @Test
    void skal_flytte_til__inngang_av_senere_steg_ved_framføring() {

        kontrollTjeneste.behandlingFramføringTilSenereBehandlingSteg(kontekst, steg5);

        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(steg5);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.UTREDES);
        assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        assertThat(behandling.getBehandlingStegTilstand(steg5)).isPresent();
        assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(behandling, steg3, BehandlingStegStatus.AVBRUTT);

        // NB: skipper STEP_4
        sjekkBehandlingStegTilstandHistorikk(behandling, steg4);

        sjekkBehandlingStegTilstandHistorikk(behandling, steg5, BehandlingStegStatus.INNGANG);

    }

    @Test
    void skal_kaste_exception_dersom_ugyldig_tilbakeføring() {
        assertThatThrownBy(() -> kontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, steg4))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Kan ikke angi steg ")
                .hasMessageContaining("som er etter");
    }

    @Test
    void skal_rykke_tilbake_til_inngang_vurderingspunkt_av_samme_steg() {

        // Arrange
        var steg = BehandlingStegType.FATTE_VEDTAK;
        forceOppdaterBehandlingSteg(behandling, steg, BehandlingStegStatus.UTGANG, BehandlingStegStatus.AVBRUTT);

        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(steg);
        assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.UTGANG);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.FATTER_VEDTAK);

        // Act
        kontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, List.of(AksjonspunktDefinisjon.FATTE_VEDTAK));

        // Assert
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(steg);
        assertThat(behandling.getStatus()).isEqualTo(BehandlingStatus.FATTER_VEDTAK);
        assertThat(behandling.getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);
        assertThat(behandling.getBehandlingStegTilstand()).isNotNull();

        assertThat(behandling.getBehandlingStegTilstandHistorikk()).hasSize(2);

        sjekkBehandlingStegTilstandHistorikk(behandling, steg, BehandlingStegStatus.INNGANG);

        assertThat(behandling.getBehandlingStegTilstand(steg).get().getBehandlingStegStatus()).isEqualTo(BehandlingStegStatus.INNGANG);

    }

    @Test
    void skal_ha_guard_mot_nøstet_behandlingskontroll_ved_prossesering_tilbakeføring_og_framføring() throws Exception {

        this.kontrollTjeneste = new BehandlingskontrollTjeneste(serviceProvider) {
            @Override
            protected BehandlingStegUtfall doProsesserBehandling(BehandlingskontrollKontekst kontekst, BehandlingModell modell,
                                                                 BehandlingModellVisitor visitor) {
                kontrollTjeneste.prosesserBehandling(kontekst);
                return null;
            }
        };

        assertThatThrownBy(() -> this.kontrollTjeneste.prosesserBehandling(kontekst))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Støtter ikke nøstet prosessering");
    }

    @Test
    void skal_returnere_true_når_aksjonspunktet_skal_løses_etter_angitt_steg() {
        assertThat(kontrollTjeneste.skalAksjonspunktLøsesIEllerEtterSteg(
                behandling.getType(), steg3, AksjonspunktDefinisjon.FATTE_VEDTAK)).isTrue();
    }

    @Test
    void skal_returnere_true_når_aksjonspunktet_skal_løses_i_angitt_steg() {
        assertThat(kontrollTjeneste.skalAksjonspunktLøsesIEllerEtterSteg(behandling.getType(), steg2, AksjonspunktDefinisjon.AVKLAR_VERGE)).isTrue();
    }

    @Test
    void skal_returnere_false_når_aksjonspunktet_skal_løses_før_angitt_steg() {
        assertThat(kontrollTjeneste.skalAksjonspunktLøsesIEllerEtterSteg(behandling.getType(), steg4, AksjonspunktDefinisjon.AVKLAR_VERGE)).isFalse();
    }

    private void sjekkBehandlingStegTilstandHistorikk(Behandling behandling, BehandlingStegType stegType,
                                                      BehandlingStegStatus... stegStatuser) {
        Assertions.assertThat(
                        behandling.getBehandlingStegTilstandHistorikk()
                                .filter(bst -> stegType == null || Objects.equals(bst.getBehandlingSteg(), stegType))
                                .map(bst -> bst.getBehandlingStegStatus()))
                .containsExactly(stegStatuser);
    }

}
