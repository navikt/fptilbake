package no.nav.foreldrepenger.tilbakekreving.behandling.steg.fattevedtak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagKonverter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;

@ExtendWith(JpaExtension.class)
class FatteVedtakStegTest {

    private BehandlingRepositoryProvider repositoryProvider;

    private BehandlingRepository behandlingRepository;

    private TotrinnRepository totrinnRepository;

    private FatteVedtakSteg fatteVedtakSteg;

    private BehandlingskontrollKontekst behandlingskontrollKontekst;

    private Behandling behandling;

    @BeforeEach
    void setup(EntityManager em) {
        repositoryProvider = new BehandlingRepositoryProvider(em);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        totrinnRepository = new TotrinnRepository(em);
        HistorikkInnslagKonverter historikkInnslagKonverter = new HistorikkInnslagKonverter(behandlingRepository);
        HistorikkTjenesteAdapter historikkTjenesteAdapter = new HistorikkTjenesteAdapter(
                repositoryProvider.getHistorikkRepository(), historikkInnslagKonverter);
        BeregningsresultatTjeneste beregningsresultatTjeneste = Mockito.mock(BeregningsresultatTjeneste.class);
        fatteVedtakSteg = new FatteVedtakSteg(repositoryProvider, totrinnRepository, beregningsresultatTjeneste,
                historikkTjenesteAdapter);

        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        repositoryProvider.getFagsakRepository().lagre(fagsak);
        behandling = lagBehandling(fagsak);
        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        behandlingskontrollKontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);

        when(beregningsresultatTjeneste.finnEllerBeregn(behandling.getId())).thenReturn(lagBeregningResultat());
    }

    @Test
    void utførSteg_medAlleGodkjenneAksjonspunkter() {
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
        assertThat(behandlingVedtak.getBehandlingsresultat().getBehandlingResultatType())
                .isEqualByComparingTo(BehandlingResultatType.DELVIS_TILBAKEBETALING);
    }

    @Test
    void utførSteg_medAvviseAksjonspunkter() {
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
        totrinnRepository.lagreOgFlush(behandling, List.of(avklartTotrinnsvurdering, foreldelseTotrinnsvurdering, vilkårTotrinnsvurdering));
    }

    private BeregningResultat lagBeregningResultat() {
        BeregningResultatPeriode periode = BeregningResultatPeriode.builder()
            .medPeriode(Periode.of(LocalDate.now(), LocalDate.now()))
            .medTilbakekrevingBeløp(BigDecimal.valueOf(5000))
            .medTilbakekrevingBeløpUtenRenter(BigDecimal.valueOf(5000))
            .medTilbakekrevingBeløpEtterSkatt(BigDecimal.valueOf(5000))
            .medSkattBeløp(BigDecimal.ZERO)
            .medRenteBeløp(BigDecimal.ZERO)
            .medFeilutbetaltBeløp(BigDecimal.valueOf(7000))
            .medUtbetaltYtelseBeløp(BigDecimal.valueOf(7000))
            .medRiktigYtelseBeløp(BigDecimal.ZERO)
            .build();

        return new BeregningResultat(VedtakResultatType.DELVIS_TILBAKEBETALING, List.of(periode));
    }
}
