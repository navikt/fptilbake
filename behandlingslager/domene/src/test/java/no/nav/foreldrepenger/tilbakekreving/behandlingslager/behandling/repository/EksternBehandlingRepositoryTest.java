package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;

@ExtendWith(JpaExtension.class)
class EksternBehandlingRepositoryTest {

    private static final UUID EKSTERN_UUID = UUID.randomUUID();

    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;

    private EksternBehandlingRepository eksternBehandlingRepository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        behandlingRepository = new BehandlingRepository(entityManager);
        fagsakRepository = new FagsakRepository(entityManager);
        eksternBehandlingRepository = new EksternBehandlingRepository(entityManager);
    }

    @Test
    void skal_lagre_ned_ekstern_behandling_data() {
        Behandling behandling = opprettBehandling();
        Long behandlingId = behandling.getId();

        EksternBehandling eksternData = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(5555L), EKSTERN_UUID);
        eksternBehandlingRepository.lagre(eksternData);

        EksternBehandling result = eksternBehandlingRepository.hentFraInternId(behandlingId);

        assertThat(result).isNotNull();
        assertThat(result.getInternId()).isEqualTo(behandlingId);
        assertThat(result.getHenvisning()).isEqualTo(Henvisning.fraEksternBehandlingId(5555L));
        assertThat(result.getEksternUuid()).isEqualTo(EKSTERN_UUID);
    }

    @Test
    void skal_hente_ekstern_data_med_ekstern_behandling_id() {
        Behandling behandling = opprettBehandling();
        Henvisning henvisning = Henvisning.fraEksternBehandlingId(5555L);

        EksternBehandling eksternBehandling = new EksternBehandling(behandling, henvisning, EKSTERN_UUID);
        eksternBehandlingRepository.lagre(eksternBehandling);

        Optional<EksternBehandling> result = eksternBehandlingRepository.hentFraHenvisning(henvisning);

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
