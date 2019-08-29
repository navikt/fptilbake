package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;

public class DokumentBestillerTestOppsett {

    protected static final long FPSAK_BEHANDLING_ID = 99051L;
    protected static final UUID FPSAK_BEHANDLING_UUID = UUID.randomUUID();
    protected static final String DUMMY_FØDSELSNUMMER = "31018143212";

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected final EntityManager entityManager = repositoryRule.getEntityManager();

    protected final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    protected final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    protected final KodeverkRepository kodeverkRepository = repositoryProvider.getKodeverkRepository();
    protected final EksternBehandlingRepository eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
    protected final BrevdataRepository brevdataRepository = new BrevdataRepositoryImpl(entityManager);


    protected Fagsak fagsak;
    protected Behandling behandling;
    protected EksternBehandling eksternBehandling;

    @Before
    public void init()  {
        fagsak = TestFagsakUtil.opprettFagsak();
        repositoryProvider.getFagsakRepository().lagre(fagsak);
        behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        eksternBehandling = new EksternBehandling(behandling, FPSAK_BEHANDLING_ID,FPSAK_BEHANDLING_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);
    }
}
