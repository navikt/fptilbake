package no.nav.foreldrepenger.tilbakekreving;

import static org.mockito.Mockito.mock;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingRevurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingHistorikkInnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagKonverter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class TestOppsett {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    protected EntityManager em = repoRule.getEntityManager();

    protected BehandlingskontrollTjeneste behandlingskontrollTjeneste = mock(BehandlingskontrollTjeneste.class);
    protected GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingTjeneste.class);
    protected BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste = mock(BehandlingskontrollAsynkTjeneste.class);
    protected TpsTjeneste mockTpsTjeneste = mock(TpsTjeneste.class);
    protected HistorikkinnslagTjeneste mockHistorikkTjeneste = mock(HistorikkinnslagTjeneste.class);
    protected FagsystemKlient mockFagsystemKlient = mock(FagsystemKlient.class);
    protected SlettGrunnlagEventPubliserer mockSlettGrunnlagEventPubliserer = mock(SlettGrunnlagEventPubliserer.class);

    protected BehandlingskontrollProvider behandlingskontrollProvider = new BehandlingskontrollProvider(behandlingskontrollTjeneste, behandlingskontrollAsynkTjeneste);

    protected BehandlingRepositoryProvider repoProvider = new BehandlingRepositoryProvider(em);
    protected NavBrukerRepository brukerRepository = new NavBrukerRepository(em);
    protected KodeverkRepository kodeverkRepository = repoProvider.getKodeverkRepository();
    protected KravgrunnlagRepository grunnlagRepository = repoProvider.getGrunnlagRepository();
    protected HistorikkRepository historikkRepository = repoProvider.getHistorikkRepository();
    protected FaktaFeilutbetalingRepository faktaFeilutbetalingRepository = repoProvider.getFaktaFeilutbetalingRepository();
    protected VurdertForeldelseRepository vurdertForeldelseRepository = repoProvider.getVurdertForeldelseRepository();
    protected VilkårsvurderingRepository vilkårsvurderingRepository = new VilkårsvurderingRepository(em);
    protected TotrinnRepository totrinnRepository = new TotrinnRepository(em);
    protected BehandlingRepository behandlingRepository = repoProvider.getBehandlingRepository();
    protected VarselRepository varselRepository = repoProvider.getVarselRepository();
    protected ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(em, null, null);
    protected KravgrunnlagTjeneste kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repoProvider, gjenopptaBehandlingTjeneste, behandlingskontrollTjeneste, mockSlettGrunnlagEventPubliserer);
    protected KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste = new KravgrunnlagBeregningTjeneste(grunnlagRepository);

    protected HistorikkInnslagKonverter historikkInnslagKonverter = new HistorikkInnslagKonverter(repoProvider.getAksjonspunktRepository());

    protected HistorikkTjenesteAdapter historikkTjenesteAdapter = new HistorikkTjenesteAdapter(historikkRepository, historikkInnslagKonverter);

    protected VurdertForeldelseTjeneste vurdertForeldelseTjeneste = new VurdertForeldelseTjeneste(repoProvider, historikkTjenesteAdapter, kravgrunnlagBeregningTjeneste);

    protected VilkårsvurderingHistorikkInnslagTjeneste vilkårsvurderingHistorikkInnslagTjeneste = new VilkårsvurderingHistorikkInnslagTjeneste(historikkTjenesteAdapter, repoProvider);

    protected VilkårsvurderingTjeneste vilkårsvurderingTjeneste = new VilkårsvurderingTjeneste(vurdertForeldelseTjeneste, repoProvider, vilkårsvurderingHistorikkInnslagTjeneste, kravgrunnlagBeregningTjeneste);

    protected BehandlingRevurderingTjeneste revurderingTjeneste = new BehandlingRevurderingTjeneste(repoProvider);

    protected FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste = new FaktaFeilutbetalingTjeneste(repoProvider, kravgrunnlagTjeneste, mockFagsystemKlient);

}
