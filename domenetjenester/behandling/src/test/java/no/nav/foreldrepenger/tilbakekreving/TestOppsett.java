package no.nav.foreldrepenger.tilbakekreving;

import static org.mockito.Mockito.mock;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.dokumentarkiv.impl.DokumentArkivTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingHistorikkInnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBrukerRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagKonverter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.simulering.tjeneste.SimuleringIntegrasjonTjeneste;
import no.nav.vedtak.felles.integrasjon.journal.v3.JournalConsumer;
import no.nav.vedtak.felles.integrasjon.journal.v3.JournalConsumerImpl;

public class TestOppsett {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    protected EntityManager em = repoRule.getEntityManager();

    protected BehandlingskontrollTjeneste behandlingskontrollTjeneste = mock(BehandlingskontrollTjeneste.class);
    protected BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste = mock(BehandlingskontrollAsynkTjeneste.class);
    protected SimuleringIntegrasjonTjeneste mockSimuleringIntegrasjonTjeneste = mock(SimuleringIntegrasjonTjeneste.class);
    protected TpsTjeneste mockTpsTjeneste = mock(TpsTjeneste.class);
    protected HistorikkinnslagTjeneste mockHistorikkTjeneste = mock(HistorikkinnslagTjeneste.class);
    protected FpsakKlient mockFpsakKlient = mock(FpsakKlient.class);
    protected JournalConsumer mockJournalConsumer = mock(JournalConsumerImpl.class);

    protected BehandlingskontrollProvider behandlingskontrollProvider = new BehandlingskontrollProvider(behandlingskontrollTjeneste, behandlingskontrollAsynkTjeneste);

    protected BehandlingRepositoryProvider repoProvider = new BehandlingRepositoryProviderImpl(em);
    protected NavBrukerRepository brukerRepository = new NavBrukerRepositoryImpl(em);
    protected KravgrunnlagRepository grunnlagRepository = repoProvider.getGrunnlagRepository();
    protected HistorikkRepository historikkRepository = repoProvider.getHistorikkRepository();
    protected FeilutbetalingRepository feilutbetalingRepository = repoProvider.getFeilutbetalingRepository();
    protected VurdertForeldelseRepository vurdertForeldelseRepository = repoProvider.getVurdertForeldelseRepository();
    protected VilkårsvurderingRepository vilkårsvurderingRepository = new VilkårsvurderingRepository(em);
    protected TotrinnRepository totrinnRepository = new TotrinnRepository(em);

    protected HistorikkInnslagKonverter historikkInnslagKonverter = new HistorikkInnslagKonverter(repoProvider.getKodeverkRepository(),
            repoProvider.getAksjonspunktRepository());

    protected DokumentArkivTjeneste dokumentArkivTjeneste = new DokumentArkivTjenesteImpl(mockJournalConsumer, repoProvider.getKodeverkRepository(),
            repoProvider.getFagsakRepository());

    protected HistorikkTjenesteAdapter historikkTjenesteAdapter = new HistorikkTjenesteAdapter(historikkRepository, historikkInnslagKonverter, dokumentArkivTjeneste);

    protected VurdertForeldelseTjeneste vurdertForeldelseTjeneste = new VurdertForeldelseTjeneste(vurdertForeldelseRepository, repoProvider, feilutbetalingRepository, historikkTjenesteAdapter);

    protected VilkårsvurderingHistorikkInnslagTjeneste vilkårsvurderingHistorikkInnslagTjeneste = new VilkårsvurderingHistorikkInnslagTjeneste(historikkTjenesteAdapter, repoProvider);

    protected VilkårsvurderingTjeneste vilkårsvurderingTjeneste = new VilkårsvurderingTjeneste(vurdertForeldelseTjeneste, repoProvider, vilkårsvurderingHistorikkInnslagTjeneste);

}
