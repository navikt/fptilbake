package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktTestSupport;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;

class VergeTjenesteTest extends FellesTestOppsett {

    private VergeTjeneste vergeTjeneste;
    private VergeRepository vergeRepository;

    @BeforeEach
    void setUp() {
        var eventPublisererMock = mock(BehandlingskontrollEventPubliserer.class);
        var svcProvider = new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), eventPublisererMock);
        var behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(svcProvider);
        vergeTjeneste = new VergeTjeneste(behandlingskontrollTjeneste, behandlingskontrollAsynkTjeneste, repoProvider);
        vergeRepository = repoProvider.getVergeRepository();
    }

    @Test
    void skal_opprette_verge_når_behandling_er_i_fakta_steg() {
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);
        vergeTjeneste.opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(behandling);
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_VERGE);
        assertThat(
                behandling.getÅpneAksjonspunkter(List.of(AksjonspunktDefinisjon.AVKLAR_VERGE))).isNotEmpty();
    }

    @Test
    void skal_opprette_verge_når_behandling_er_etter_fakta_steg() {
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);
        vergeTjeneste.opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(behandling);
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_VERGE);
        assertThat(
                behandling.getÅpneAksjonspunkter(List.of(AksjonspunktDefinisjon.AVKLAR_VERGE))).isNotEmpty();
    }

    @Test
    void skal_fjerne_verge() {
        entityManager.setFlushMode(FlushModeType.AUTO);
        VergeEntitet vergeEntitet = VergeEntitet.builder()
                .medVergeAktørId(behandling.getAktørId())
                .medVergeType(VergeType.BARN)
                .medKilde(KildeType.FPTILBAKE.name())
                .medNavn("John Doe")
                .medGyldigPeriode(FOM, TOM)
                .medBegrunnelse("begunnlese")
                .build();
        vergeRepository.lagreVergeInformasjon(internBehandlingId, vergeEntitet);
        AksjonspunktTestSupport.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_VERGE, BehandlingStegType.FAKTA_VERGE);
        assertThat(vergeRepository.finnVergeInformasjon(internBehandlingId)).isNotEmpty();
        assertThat(
                behandling.getÅpneAksjonspunkter(List.of(AksjonspunktDefinisjon.AVKLAR_VERGE))).isNotEmpty();
        vergeTjeneste.fjernVergeGrunnlagOgAksjonspunkt(behandling);

        assertThat(vergeRepository.finnVergeInformasjon(internBehandlingId)).isEmpty();
        assertThat(behandling.getÅpneAksjonspunkter(List.of(AksjonspunktDefinisjon.AVKLAR_VERGE))).isEmpty();
        Optional<Aksjonspunkt> aksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(
                AksjonspunktDefinisjon.AVKLAR_VERGE);
        assertThat(aksjonspunkt).isPresent();
        assertThat(aksjonspunkt.get().erAvbrutt()).isTrue();
        List<HistorikkinnslagOld> historikkinnslager = historikkRepository.hentHistorikk(internBehandlingId);
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(1);
        assertThat(historikkinnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.FJERNET_VERGE);
        assertThat(historikkinnslager.get(0).getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
    }

}
