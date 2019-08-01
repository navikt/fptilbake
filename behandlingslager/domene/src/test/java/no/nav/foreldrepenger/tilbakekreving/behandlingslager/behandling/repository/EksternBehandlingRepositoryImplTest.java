package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;

public class EksternBehandlingRepositoryImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepository behandlingRepository = new BehandlingRepositoryImpl(repoRule.getEntityManager());
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repoRule.getEntityManager());

    private EksternBehandlingRepository eksternBehandlingRepository = new EksternBehandlingRepositoryImpl(repoRule.getEntityManager());

    @Test
    public void skal_lagre_ned_ekstern_behandling_data() {
        Behandling behandling = opprettBehandling();
        Long behandlingId = behandling.getId();

        EksternBehandling eksternData = new EksternBehandling(behandling, 5555L);
        eksternBehandlingRepository.lagre(eksternData);

        EksternBehandling result = eksternBehandlingRepository.hentFraInternId(behandlingId);

        assertThat(result).isNotNull();
        assertThat(result.getInternId()).isEqualTo(behandlingId);
        assertThat(result.getEksternId()).isEqualTo(5555L);
    }

    @Test
    public void skal_hente_ekstern_data_med_ekstern_behandling_id() {
        Behandling behandling = opprettBehandling();
        long eksternId = 5555L;

        EksternBehandling eksternBehandling = new EksternBehandling(behandling, eksternId);
        eksternBehandlingRepository.lagre(eksternBehandling);

        Optional<EksternBehandling> result = eksternBehandlingRepository.hentFraEksternId(eksternId);

        assertThat(result).hasValue(eksternBehandling);
    }

    private Behandling opprettBehandling() {
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);

        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
        return behandling;
    }

}
