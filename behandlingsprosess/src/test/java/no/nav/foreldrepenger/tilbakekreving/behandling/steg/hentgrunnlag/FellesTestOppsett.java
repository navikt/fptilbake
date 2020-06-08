package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.FlushModeType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.LesKravgrunnlagTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.MottattGrunnlagStegImpl;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.IverksetteVedtakStegImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEventPubliserer;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class FellesTestOppsett {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected static final Long FPSAK_BEHANDLING_ID = 100000001L;
    protected static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(FPSAK_BEHANDLING_ID);
    protected static final UUID FPSAK_BEHANDLING_UUID = UUID.randomUUID();

    private ProsessTaskEventPubliserer eventPublisererMock = mock(ProsessTaskEventPubliserer.class);
    private BehandlingskontrollEventPubliserer behandlingskontrollEventPublisererMock = mock(BehandlingskontrollEventPubliserer.class);
    protected final TpsAdapter tpsAdapterMock = mock(TpsAdapter.class);
    private PersoninfoAdapter personinfoAdapterMock = mock(PersoninfoAdapter.class);
    protected final FagsystemKlient fagsystemKlientMock = mock(FagsystemKlient.class);
    protected final TpsAdapterWrapper tpsAdapterWrapper = new TpsAdapterWrapper(tpsAdapterMock);
    private BehandlingModellRepository behandlingModellRepositoryMock = mock(BehandlingModellRepository.class);
    private VarselresponsTjeneste varselresponsTjenesteMock = mock(VarselresponsTjeneste.class);
    private SlettGrunnlagEventPubliserer mockSlettGrunnlagEventPubliserer = mock(SlettGrunnlagEventPubliserer.class);

    protected final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    protected final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    protected final FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
    protected final KravgrunnlagRepository grunnlagRepository = repositoryProvider.getGrunnlagRepository();
    protected final ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null, eventPublisererMock);
    protected final ØkonomiMottattXmlRepository mottattXmlRepository = new ØkonomiMottattXmlRepository(repoRule.getEntityManager());
    protected final EksternBehandlingRepository eksternBehandlingRepository = new EksternBehandlingRepositoryImpl(repoRule.getEntityManager());
    private final FellesQueriesForBehandlingRepositories fellesQueriesForBehandlingRepositories = new FellesQueriesForBehandlingRepositories(repoRule.getEntityManager());
    protected final BehandlingVenterRepository behandlingVenterRepository = new BehandlingVenterRepositoryImpl(fellesQueriesForBehandlingRepositories);
    protected final BehandlingKandidaterRepository behandlingKandidaterRepository = new BehandlingKandidaterRepository(fellesQueriesForBehandlingRepositories);

    protected final GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste = new GjenopptaBehandlingTjenesteImpl(prosessTaskRepository, behandlingKandidaterRepository, behandlingVenterRepository, repositoryProvider, varselresponsTjenesteMock);
    protected final HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(repositoryProvider.getHistorikkRepository(), personinfoAdapterMock);
    protected final BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, behandlingModellRepositoryMock, behandlingskontrollEventPublisererMock);
    private InternalManipulerBehandling manipulerInternBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);
    protected final KravgrunnlagTjeneste kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repositoryProvider, prosessTaskRepository, gjenopptaBehandlingTjeneste, behandlingskontrollTjeneste, mockSlettGrunnlagEventPubliserer);
    protected final KravgrunnlagMapper kravgrunnlagMapper = new KravgrunnlagMapper(tpsAdapterWrapper);
    protected final LesKravgrunnlagTask lesKravgrunnlagTask = new LesKravgrunnlagTask(mottattXmlRepository, kravgrunnlagTjeneste, kravgrunnlagMapper, repositoryProvider, fagsystemKlientMock);

    protected final KravgrunnlagMapper mapper = new KravgrunnlagMapper(tpsAdapterWrapper);


    public Fagsak fagsak;
    public Behandling behandling;

    @Before
    public void init() {
        repoRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        behandling = lagBehandling(null);
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG);

        when(tpsAdapterMock.hentAktørIdForPersonIdent(any(PersonIdent.class))).thenReturn(Optional.of(fagsak.getAktørId()));
        when(behandlingModellRepositoryMock.getBehandlingStegKonfigurasjon()).thenReturn(BehandlingStegKonfigurasjon.lagDummy());
        when(behandlingModellRepositoryMock.getModell(any(BehandlingType.class))).thenReturn(lagDummyBehandlingsModell());

    }

    public String getInputXML(String filename) {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError("Feil i testoppsett", e);
        }
    }

    public Behandling lagBehandling(BehandlingÅrsak.Builder builder) {
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.REVURDERING_TILBAKEKREVING)
            .medBehandlingÅrsak(builder).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Long behandlingId = behandlingRepository.lagre(behandling, lås);
        return behandlingRepository.hentBehandling(behandlingId);
    }

    public ProsessTaskData lagProsessTaskData(Long mottattXmlId, String taskType) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(taskType);
        prosessTaskData.setProperty(TaskProperty.PROPERTY_MOTTATT_XML_ID, String.valueOf(mottattXmlId));
        return prosessTaskData;
    }

    private BehandlingModell lagDummyBehandlingsModell() {
        List<TestStegKonfig> steg = Lists.newArrayList(
            new TestStegKonfig(BehandlingStegType.TBKGSTEG, BehandlingType.TILBAKEKREVING, new MottattGrunnlagStegImpl()),
            new TestStegKonfig(BehandlingStegType.IVERKSETT_VEDTAK, BehandlingType.TILBAKEKREVING, new IverksetteVedtakStegImpl(repositoryProvider, null)));

        BehandlingModellImpl.TriFunction<BehandlingStegType, BehandlingType, BehandlingSteg> finnSteg = map(steg);
        BehandlingModellImpl modell = new BehandlingModellImpl(BehandlingType.TILBAKEKREVING, finnSteg);

        steg.forEach(konfig -> modell.leggTil(konfig.getBehandlingStegType(), konfig.getBehandlingType()));
        return modell;
    }

    private static BehandlingModellImpl.TriFunction<BehandlingStegType, BehandlingType, BehandlingSteg> map(List<TestStegKonfig> input) {
        Map<List<?>, BehandlingSteg> resolver = new HashMap<>();
        for (TestStegKonfig konfig : input) {
            List<?> key = Arrays.asList(konfig.getBehandlingStegType(), konfig.getBehandlingType());
            resolver.put(key, konfig.getSteg());
        }
        BehandlingModellImpl.TriFunction<BehandlingStegType, BehandlingType, BehandlingSteg> func = (t, u) -> resolver.get(Arrays.asList(t, u));
        return func;
    }
}
