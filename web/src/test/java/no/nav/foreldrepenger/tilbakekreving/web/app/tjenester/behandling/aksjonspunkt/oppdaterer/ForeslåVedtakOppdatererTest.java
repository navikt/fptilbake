package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.ForeslåVedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VedtaksbrevFritekstTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VedtaksbrevFritekstValidator;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslåVedtakDto;

@ExtendWith(JpaExtension.class)
class ForeslåVedtakOppdatererTest {

    private EntityManager entityManager;
    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;
    private VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository;

    private ForeslåVedtakOppdaterer foreslåVedtakOppdaterer;

    @BeforeEach
    void setup(EntityManager entityManager) {
        this.entityManager = entityManager;
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        var eventPublisererMock = mock(BehandlingskontrollEventPubliserer.class);
        var svcProvider = new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), eventPublisererMock);
        var behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(svcProvider);
        vedtaksbrevFritekstRepository = new VedtaksbrevFritekstRepository(entityManager);
        BeregningsresultatTjeneste beregningsresultatTjenesteMock = mock(BeregningsresultatTjeneste.class);
        HistorikkTjenesteAdapter historikkTjenesteAdapterMock = mock(HistorikkTjenesteAdapter.class);
        ForeslåVedtakTjeneste foreslåVedtakTjeneste = new ForeslåVedtakTjeneste(beregningsresultatTjenesteMock, historikkTjenesteAdapterMock);
        TotrinnTjeneste totrinnTjenesteMock = mock(TotrinnTjeneste.class);
        VilkårsvurderingRepository vilkårsvurderingRepository = mock(VilkårsvurderingRepository.class);
        FaktaFeilutbetalingRepository faktaFeilutbetalingRepository = mock(FaktaFeilutbetalingRepository.class);
        VedtaksbrevFritekstValidator vedtaksbrevFritekstValidator = new VedtaksbrevFritekstValidator(faktaFeilutbetalingRepository, vilkårsvurderingRepository, behandlingRepository);
        VedtaksbrevFritekstTjeneste vedtaksbrevFritekstTjeneste = new VedtaksbrevFritekstTjeneste(vedtaksbrevFritekstValidator, vedtaksbrevFritekstRepository);
        foreslåVedtakOppdaterer = new ForeslåVedtakOppdaterer(foreslåVedtakTjeneste, totrinnTjenesteMock, behandlingskontrollTjeneste, vedtaksbrevFritekstTjeneste);

        when(historikkTjenesteAdapterMock.tekstBuilder()).thenReturn(new HistorikkInnslagTekstBuilder());
        when(beregningsresultatTjenesteMock.finnEllerBeregn(anyLong())).thenReturn(new BeregningResultat(VedtakResultatType.FULL_TILBAKEBETALING, List.of()));
        when(faktaFeilutbetalingRepository.finnFaktaOmFeilutbetaling(anyLong())).thenReturn(Optional.of(new FaktaFeilutbetaling()));
    }

    @Test
    void oppdater_medFaktaAvsnitt() {
        Behandling behandling = lagMockBehandling();
        foreslåVedtakOppdaterer.oppdater(lagMockForeslåVedtak("fakta", null, null), behandling);
        entityManager.flush();

        fellesAssert(behandling);
        List<VedtaksbrevFritekstPeriode> perioder = vedtaksbrevFritekstRepository.hentVedtaksbrevPerioderMedTekst(behandling.getId());
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(1);
        VedtaksbrevFritekstPeriode periode = perioder.get(0);
        assertThat(periode.getFritekstType()).isEqualByComparingTo(VedtaksbrevFritekstType.FAKTA_AVSNITT);
        assertThat(periode.getFritekst()).isEqualTo("fakta");
        assertThat(periode.getBehandlingId()).isEqualTo(behandling.getId());
    }

    @Test
    void oppdater_medVilkårAvsnitt() {
        Behandling behandling = lagMockBehandling();
        foreslåVedtakOppdaterer.oppdater(lagMockForeslåVedtak(null, null, "vilkår"), behandling);
        entityManager.flush();

        fellesAssert(behandling);
        List<VedtaksbrevFritekstPeriode> perioder = vedtaksbrevFritekstRepository.hentVedtaksbrevPerioderMedTekst(behandling.getId());
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(1);
        VedtaksbrevFritekstPeriode periode = perioder.get(0);
        assertThat(periode.getFritekstType()).isEqualByComparingTo(VedtaksbrevFritekstType.VILKAAR_AVSNITT);
        assertThat(periode.getFritekst()).isEqualTo("vilkår");
        assertThat(periode.getBehandlingId()).isEqualTo(behandling.getId());
    }

    @Test
    void oppdater_medSærligGrunnerAvsnitt() {
        Behandling behandling = lagMockBehandling();
        foreslåVedtakOppdaterer.oppdater(lagMockForeslåVedtak(null, "særligGrunner", null), behandling);
        entityManager.flush();

        fellesAssert(behandling);
        List<VedtaksbrevFritekstPeriode> perioder = vedtaksbrevFritekstRepository.hentVedtaksbrevPerioderMedTekst(behandling.getId());
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(1);
        VedtaksbrevFritekstPeriode periode = perioder.get(0);
        assertThat(periode.getFritekstType()).isEqualByComparingTo(VedtaksbrevFritekstType.SAERLIGE_GRUNNER_AVSNITT);
        assertThat(periode.getFritekst()).isEqualTo("særligGrunner");
        assertThat(periode.getBehandlingId()).isEqualTo(behandling.getId());
    }

    private void fellesAssert(Behandling behandling) {
        assertThat(behandling.getAksjonspunkter()).isNotEmpty();
        assertThat(behandling.getAksjonspunkter().stream()
                .filter(aksjonspunkt -> aksjonspunkt.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.FATTE_VEDTAK))
                .findFirst()).isNotEmpty();
    }

    private Behandling lagMockBehandling() {
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        repositoryProvider.getFagsakRepository().lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        return behandling;
    }

    private ForeslåVedtakDto lagMockForeslåVedtak(String faktaAvsnitt, String særligGrunnerAvsnitt, String vilkårAvsnitt) {
        ForeslåVedtakDto foreslåVedtakDto = new ForeslåVedtakDto();
        foreslåVedtakDto.setOppsummeringstekst("oppsummering");
        PeriodeMedTekstDto periodeMedTekstDto = new PeriodeMedTekstDto();
        periodeMedTekstDto.setFaktaAvsnitt(faktaAvsnitt);
        periodeMedTekstDto.setSærligeGrunnerAvsnitt(særligGrunnerAvsnitt);
        periodeMedTekstDto.setVilkårAvsnitt(vilkårAvsnitt);
        periodeMedTekstDto.setFom(LocalDate.of(2016, 3, 16));
        periodeMedTekstDto.setTom(LocalDate.of(2016, 5, 26));
        foreslåVedtakDto.setPerioderMedTekst(Lists.newArrayList(periodeMedTekstDto));
        return foreslåVedtakDto;
    }

}
