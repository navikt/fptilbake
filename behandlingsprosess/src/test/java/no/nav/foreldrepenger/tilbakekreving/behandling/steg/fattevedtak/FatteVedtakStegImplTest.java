package no.nav.foreldrepenger.tilbakekreving.behandling.steg.fattevedtak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class FatteVedtakStegImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private EntityManager em = repositoryRule.getEntityManager();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(em);

    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private TotrinnRepository totrinnRepository = new TotrinnRepository(em);

    private TilbakekrevingBeregningTjeneste beregningTjeneste = Mockito.mock(TilbakekrevingBeregningTjeneste.class);

    private FatteVedtakStegImpl fatteVedtakSteg = new FatteVedtakStegImpl(repositoryProvider, totrinnRepository, beregningTjeneste);

    private BehandlingskontrollKontekst behandlingskontrollKontekst;

    private Behandling behandling;

    @Before
    public void setup() {
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        repositoryProvider.getFagsakRepository().lagre(fagsak);
        behandling = lagBehandling(fagsak);
        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        behandlingskontrollKontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);

        when(beregningTjeneste.beregn(behandling.getId())).thenReturn(lagBeregningResultat());
    }

    @Test
    public void utførSteg_medAlleGodkjenneAksjonspunkter() {
        Map<AksjonspunktDefinisjon, Boolean> aksjonspunktMedGodkjentMap = new HashMap<>();
        aksjonspunktMedGodkjentMap.put(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, true);
        aksjonspunktMedGodkjentMap.put(AksjonspunktDefinisjon.VURDER_FORELDELSE, true);
        aksjonspunktMedGodkjentMap.put(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, true);
        lagTotrinnsVurderinger(aksjonspunktMedGodkjentMap);

        BehandleStegResultat behandleStegResultat = fatteVedtakSteg.utførSteg(behandlingskontrollKontekst);
        assertThat(behandleStegResultat).isNotNull();
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        Optional<BehandlingVedtak> vedtak = repositoryProvider.getBehandlingVedtakRepository()
            .hentBehandlingvedtakForBehandlingId(behandling.getId());
        assertThat(vedtak).isPresent();
        BehandlingVedtak behandlingVedtak = vedtak.get();
        assertThat(behandlingVedtak.getIverksettingStatus()).isEqualByComparingTo(IverksettingStatus.IKKE_IVERKSATT);
        assertThat(behandlingVedtak.getVedtakResultatType()).isEqualByComparingTo(VedtakResultatType.DELVIS_TILBAKEBETALING);
        assertThat(behandlingVedtak.getBehandlingsresultat().getBehandlingResultatType()).isEqualByComparingTo(BehandlingResultatType.FASTSATT);
    }

    @Test
    public void utførSteg_medAvviseAksjonspunkter() {
        Map<AksjonspunktDefinisjon, Boolean> aksjonspunktMedGodkjentMap = new HashMap<>();
        aksjonspunktMedGodkjentMap.put(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, true);
        aksjonspunktMedGodkjentMap.put(AksjonspunktDefinisjon.VURDER_FORELDELSE, false);
        aksjonspunktMedGodkjentMap.put(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING, false);
        lagTotrinnsVurderinger(aksjonspunktMedGodkjentMap);

        BehandleStegResultat behandleStegResultat = fatteVedtakSteg.utførSteg(behandlingskontrollKontekst);
        assertThat(behandleStegResultat).isNotNull();
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.TILBAKEFØRT_TIL_AKSJONSPUNKT);
        Optional<BehandlingVedtak> vedtak = repositoryProvider.getBehandlingVedtakRepository()
            .hentBehandlingvedtakForBehandlingId(behandling.getId());
        assertThat(vedtak).isEmpty();
    }

    private Behandling lagBehandling(Fagsak fagsak) {
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Long behandlingId = behandlingRepository.lagre(behandling, lås);
        return behandlingRepository.hentBehandling(behandlingId);
    }

    private void lagTotrinnsVurderinger(Map<AksjonspunktDefinisjon, Boolean> aksjonspunktMedGodkjentMap) {
        Totrinnsvurdering avklartTotrinnsvurdering = Totrinnsvurdering.builder()
            .medBehandling(behandling)
            .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING)
            .medGodkjent(aksjonspunktMedGodkjentMap.get(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING))
            .build();
        Totrinnsvurdering foreldelseTotrinnsvurdering = Totrinnsvurdering.builder()
            .medBehandling(behandling)
            .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FORELDELSE)
            .medGodkjent(aksjonspunktMedGodkjentMap.get(AksjonspunktDefinisjon.VURDER_FORELDELSE))
            .build();
        Totrinnsvurdering vilkårTotrinnsvurdering = Totrinnsvurdering.builder()
            .medBehandling(behandling)
            .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING)
            .medGodkjent(aksjonspunktMedGodkjentMap.get(AksjonspunktDefinisjon.VURDER_TILBAKEKREVING))
            .build();
        totrinnRepository.lagreOgFlush(behandling, Lists.newArrayList(avklartTotrinnsvurdering, foreldelseTotrinnsvurdering, vilkårTotrinnsvurdering));
    }

    private BeregningResultat lagBeregningResultat() {
        BeregningResultat beregningResultat = new BeregningResultat();
        BeregningResultatPeriode periode = new BeregningResultatPeriode();
        periode.setTilbakekrevingBeløp(BigDecimal.valueOf(5000.00));
        periode.setFeilutbetaltBeløp(BigDecimal.valueOf(7000.00));
        periode.setVurdering(AnnenVurdering.GOD_TRO);
        beregningResultat.setVedtakResultatType(VedtakResultatType.DELVIS_TILBAKEBETALING);
        beregningResultat.setBeregningResultatPerioder(Lists.newArrayList(periode));

        return beregningResultat;
    }
}
