package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import javax.persistence.FlushModeType;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class VergeTjenesteTest extends FellesTestOppsett {
    private BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepositoryImpl(repoRule.getEntityManager());
    private BehandlingskontrollEventPubliserer eventPublisererMock = mock(BehandlingskontrollEventPubliserer.class);
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repoProvider, behandlingModellRepository, eventPublisererMock);
    private VergeTjeneste vergeTjeneste = new VergeTjeneste(behandlingskontrollTjeneste, gjenopptaBehandlingTjeneste,
        repoProvider);
    private InternalManipulerBehandling manipulerBehandling = new InternalManipulerBehandlingImpl(repoProvider);
    private VergeRepository vergeRepository = repoProvider.getVergeRepository();

    @Test
    public void skal_opprette_verge_når_behandling_er_i_fakta_steg() {
        manipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);
        vergeTjeneste.opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(behandling);
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_VERGE);
        assertThat(behandling.getÅpneAksjonspunkter(Lists.newArrayList(AksjonspunktDefinisjon.AVKLAR_VERGE))).isNotEmpty();
    }

    @Test
    public void skal_opprette_verge_når_behandling_er_etter_fakta_steg() {
        manipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VTILBSTEG);
        vergeTjeneste.opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(behandling);
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_VERGE);
        assertThat(behandling.getÅpneAksjonspunkter(Lists.newArrayList(AksjonspunktDefinisjon.AVKLAR_VERGE))).isNotEmpty();
    }

    @Test
    public void skal_fjerne_verge() {
        repoRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        VergeEntitet vergeEntitet = VergeEntitet.builder().medVergeAktørId(behandling.getAktørId())
            .medVergeType(VergeType.BARN)
            .medKilde(KildeType.FPTILBAKE.name())
            .medNavn("John Doe")
            .medGyldigPeriode(FOM, TOM).build();
        vergeRepository.lagreVergeInformasjon(internBehandlingId, vergeEntitet);
        repoProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_VERGE, BehandlingStegType.FAKTA_VERGE);
        assertThat(vergeRepository.finnVergeInformasjon(internBehandlingId)).isNotEmpty();
        assertThat(behandling.getÅpneAksjonspunkter(Lists.newArrayList(AksjonspunktDefinisjon.AVKLAR_VERGE))).isNotEmpty();
        vergeTjeneste.fjernVergeGrunnlagOgAksjonspunkt(behandling);

        assertThat(vergeRepository.finnVergeInformasjon(internBehandlingId)).isEmpty();
        assertThat(behandling.getÅpneAksjonspunkter(Lists.newArrayList(AksjonspunktDefinisjon.AVKLAR_VERGE))).isEmpty();
        Optional<Aksjonspunkt> aksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.AVKLAR_VERGE);
        assertThat(aksjonspunkt).isPresent();
        assertThat(aksjonspunkt.get().erAvbrutt()).isTrue();
        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(internBehandlingId);
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(1);
        assertThat(historikkinnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.FJERNET_VERGE);
        assertThat(historikkinnslager.get(0).getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
    }

}
