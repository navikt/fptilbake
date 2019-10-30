package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.ForeslåVedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VedtaksbrevFritekstTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.FritekstType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.PeriodeMedTekstDto;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslåVedtakDto;

public class ForeslåVedtakOppdatererTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    private BrevdataRepository brevdataRepository = new BrevdataRepositoryImpl(repositoryRule.getEntityManager());

    private TilbakekrevingBeregningTjeneste beregningTjenesteMock = mock(TilbakekrevingBeregningTjeneste.class);
    private HistorikkTjenesteAdapter historikkTjenesteAdapterMock = mock(HistorikkTjenesteAdapter.class);
    private ForeslåVedtakTjeneste foreslåVedtakTjeneste = new ForeslåVedtakTjeneste(beregningTjenesteMock, historikkTjenesteAdapterMock);
    private TotrinnTjeneste totrinnTjenesteMock = mock(TotrinnTjeneste.class);
    private VilkårsvurderingRepository vilkårsvurderingRepository = mock(VilkårsvurderingRepository.class);

    private final VedtaksbrevFritekstTjeneste vedtaksbrevFritekstTjeneste = new VedtaksbrevFritekstTjeneste(vilkårsvurderingRepository, brevdataRepository);
    private ForeslåVedtakOppdaterer foreslåVedtakOppdaterer = new ForeslåVedtakOppdaterer(foreslåVedtakTjeneste, totrinnTjenesteMock, aksjonspunktRepository, vedtaksbrevFritekstTjeneste);

    @Before
    public void setup() {
        when(historikkTjenesteAdapterMock.tekstBuilder()).thenReturn(new HistorikkInnslagTekstBuilder());
        when(beregningTjenesteMock.beregn(anyLong())).thenReturn(new BeregningResultat());
    }

    @Test
    public void oppdater_medFaktaAvsnitt() {
        Behandling behandling = lagMockBehandling();
        foreslåVedtakOppdaterer.oppdater(lagMockForeslåVedtak("fakta", null, null), behandling);

        fellesAssert(behandling);
        List<VedtaksbrevPeriode> perioder = brevdataRepository.hentVedtaksbrevPerioderMedTekst(behandling.getId());
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(1);
        VedtaksbrevPeriode periode = perioder.get(0);
        assertThat(periode.getFritekstType()).isEqualByComparingTo(FritekstType.FAKTA_AVSNITT);
        assertThat(periode.getFritekst()).isEqualTo("fakta");
        assertThat(periode.getBehandlingId()).isEqualTo(behandling.getId());
    }

    @Test
    public void oppdater_medVilkårAvsnitt() {
        Behandling behandling = lagMockBehandling();
        foreslåVedtakOppdaterer.oppdater(lagMockForeslåVedtak(null, null, "vilkår"), behandling);

        fellesAssert(behandling);
        List<VedtaksbrevPeriode> perioder = brevdataRepository.hentVedtaksbrevPerioderMedTekst(behandling.getId());
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(1);
        VedtaksbrevPeriode periode = perioder.get(0);
        assertThat(periode.getFritekstType()).isEqualByComparingTo(FritekstType.VILKAAR_AVSNITT);
        assertThat(periode.getFritekst()).isEqualTo("vilkår");
        assertThat(periode.getBehandlingId()).isEqualTo(behandling.getId());
    }

    @Test
    public void oppdater_medSærligGrunnerAvsnitt() {
        Behandling behandling = lagMockBehandling();
        foreslåVedtakOppdaterer.oppdater(lagMockForeslåVedtak(null, "særligGrunner", null), behandling);

        fellesAssert(behandling);
        List<VedtaksbrevPeriode> perioder = brevdataRepository.hentVedtaksbrevPerioderMedTekst(behandling.getId());
        assertThat(perioder).isNotEmpty();
        assertThat(perioder.size()).isEqualTo(1);
        VedtaksbrevPeriode periode = perioder.get(0);
        assertThat(periode.getFritekstType()).isEqualByComparingTo(FritekstType.SAERLIGE_GRUNNER_AVSNITT);
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

