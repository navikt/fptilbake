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
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.GjenopptaBehandlingMedGrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.LesKravgrunnlagTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(JpaExtension.class)
public abstract class FellesTestOppsett {

    protected static final Long FPSAK_BEHANDLING_ID = 100000001L;
    protected static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(FPSAK_BEHANDLING_ID);
    protected static final UUID FPSAK_BEHANDLING_UUID = UUID.randomUUID();

    private final BehandlingskontrollEventPubliserer behandlingskontrollEventPublisererMock = mock(
            BehandlingskontrollEventPubliserer.class);
    protected PersoninfoAdapter personinfoAdapterMock = mock(PersoninfoAdapter.class);
    protected final FagsystemKlient fagsystemKlientMock = mock(FagsystemKlient.class);
    protected final PersonOrganisasjonWrapper tpsAdapterWrapper = new PersonOrganisasjonWrapper(personinfoAdapterMock);
    private final SlettGrunnlagEventPubliserer mockSlettGrunnlagEventPubliserer = mock(
            SlettGrunnlagEventPubliserer.class);

    protected BehandlingRepositoryProvider repositoryProvider;
    protected BehandlingRepository behandlingRepository;
    protected FagsakRepository fagsakRepository;
    protected KravgrunnlagRepository grunnlagRepository;
    protected ProsessTaskTjeneste taskTjeneste;
    protected ØkonomiMottattXmlRepository mottattXmlRepository;
    protected EksternBehandlingRepository eksternBehandlingRepository;
    protected BehandlingVenterRepository behandlingVenterRepository;
    protected BehandlingKandidaterRepository behandlingKandidaterRepository;

    protected GjenopptaBehandlingMedGrunnlagTjeneste gjenopptaBehandlingTjeneste;
    protected HistorikkinnslagTjeneste historikkinnslagTjeneste;
    protected BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    protected KravgrunnlagTjeneste kravgrunnlagTjeneste;
    protected KravgrunnlagMapper kravgrunnlagMapper;
    protected LesKravgrunnlagTask lesKravgrunnlagTask;

    protected final KravgrunnlagMapper mapper = new KravgrunnlagMapper(tpsAdapterWrapper);

    protected EntityManager entityManager;

    public Fagsak fagsak;
    public Behandling behandling;

    //BeforeEach kalles både her og i subklasse
    @BeforeEach
    public final void init(EntityManager entityManager) {
        this.entityManager = entityManager;
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        fagsakRepository = repositoryProvider.getFagsakRepository();
        grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        taskTjeneste = Mockito.mock(ProsessTaskTjeneste.class);
        mottattXmlRepository = new ØkonomiMottattXmlRepository(entityManager);
        eksternBehandlingRepository = new EksternBehandlingRepository(entityManager);
        var fellesQueriesForBehandlingRepositories = new FellesQueriesForBehandlingRepositories(
                entityManager);
        behandlingVenterRepository = new BehandlingVenterRepository(fellesQueriesForBehandlingRepositories);
        behandlingKandidaterRepository = new BehandlingKandidaterRepository(fellesQueriesForBehandlingRepositories);
        gjenopptaBehandlingTjeneste = new GjenopptaBehandlingMedGrunnlagTjeneste(taskTjeneste, behandlingVenterRepository);
        historikkinnslagTjeneste = new HistorikkinnslagTjeneste(repositoryProvider.getHistorikkRepository()
        );
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager,
                new BehandlingModellRepository(), behandlingskontrollEventPublisererMock));
        kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repositoryProvider, gjenopptaBehandlingTjeneste,
                behandlingskontrollTjeneste, mockSlettGrunnlagEventPubliserer, entityManager);
        kravgrunnlagMapper = new KravgrunnlagMapper(tpsAdapterWrapper);
        lesKravgrunnlagTask = new LesKravgrunnlagTask(mottattXmlRepository, kravgrunnlagTjeneste, kravgrunnlagMapper,
                repositoryProvider, fagsystemKlientMock);

        entityManager.setFlushMode(FlushModeType.AUTO);
        fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        behandling = lagBehandling(null);
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.HENTGRUNNLAGSTEG);

        when(personinfoAdapterMock.hentAktørForFnr(any(PersonIdent.class))).thenReturn(
                Optional.of(fagsak.getAktørId()));

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
                .medBehandlingÅrsak(builder)
                .build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Long behandlingId = behandlingRepository.lagre(behandling, lås);
        return behandlingRepository.hentBehandling(behandlingId);
    }

    public ProsessTaskData lagProsessTaskData(Long mottattXmlId, TaskType taskType) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forTaskType(taskType);
        prosessTaskData.setProperty(TaskProperties.PROPERTY_MOTTATT_XML_ID, String.valueOf(mottattXmlId));
        return prosessTaskData;
    }
}
