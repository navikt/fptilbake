package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@ExtendWith(JpaExtension.class)
class BehandlingRepositoryTest {

    private final NavBruker bruker = TestFagsakUtil.genererBruker();
    private final Fagsak fagsak = TestFagsakUtil.opprettFagsak(bruker);

    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        fagsakRepository = new FagsakRepository(entityManager);
    }

    @Test
    void skal_finne_behandling_gitt_id() {

        // Arrange
        Behandling behandling = opprettBuilderForBehandling().build();
        lagreBehandling(behandling);

        // Act
        Behandling resultat = behandlingRepository.hentBehandling(behandling.getId());

        // Assert
        assertThat(resultat).isNotNull();
    }

    @Test
    void skal_finne_behandlinger_på_saksnummer() {
        Fagsak fagsak1 = Fagsak.opprettNy(new Saksnummer("GSAK1000"), bruker);
        Fagsak fagsak2 = Fagsak.opprettNy(new Saksnummer("GSAK1001"), bruker);
        fagsakRepository.lagre(fagsak1);
        fagsakRepository.lagre(fagsak2);

        Behandling behandling1a = Behandling.nyBehandlingFor(fagsak1, BehandlingType.TILBAKEKREVING).build();
        Behandling behandling1b = Behandling.nyBehandlingFor(fagsak1, BehandlingType.TILBAKEKREVING).build();
        Behandling behandling2 = Behandling.nyBehandlingFor(fagsak2, BehandlingType.TILBAKEKREVING).build();
        lagreBehandling(behandling1a, behandling1b, behandling2);

        List<Behandling> resultat = behandlingRepository.hentAlleBehandlingerForSaksnummer(new Saksnummer("GSAK1000"));

        assertThat(resultat).containsOnly(behandling1a, behandling1b);
    }

    private void lagreBehandling(Behandling... behandlinger) {
        for (Behandling behandling : behandlinger) {
            BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
            behandlingRepository.lagre(behandling, lås);
        }
    }


    private void lagreBehandling(Behandling.Builder builder) {
        Behandling behandling = builder.build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
    }


    @Test
    void skal_hente_siste_behandling_basert_på_fagsakId() {

        Behandling.Builder builder = opprettBuilderForBehandling();

        lagreBehandling(builder);

        Optional<Behandling> sisteBehandling = behandlingRepository.hentSisteBehandlingForFagsakId(fagsak.getId(),
                BehandlingType.TILBAKEKREVING);

        assertThat(sisteBehandling).isPresent();
        assertThat(sisteBehandling.get().getFagsakId()).isEqualTo(fagsak.getId());

    }

    @Test
    void skal_hente_behandlinger_som_ikke_er_avsluttet() {
        Map<String, Object> map = opprettBuilderForBehandlingMedFagsakId();

        Behandling.Builder builder = ((Behandling.Builder) map.get("builder"));
        Long fagsakId = ((Long) map.get("fagsakId"));

        lagreBehandling(builder);

        List<Behandling> resultat = behandlingRepository.hentBehandlingerSomIkkeErAvsluttetForFagsakId(fagsakId);
        assertThat(resultat).isNotEmpty();
        assertThat(resultat).hasSize(1);
    }

    @Test
    void skal_ikke_hente_avsluttede_behandlinger() {
        Map<String, Object> map = opprettBuilderForBehandlingMedFagsakId();
        Behandling.Builder builder = ((Behandling.Builder) map.get("builder"));
        Long fagsakId = ((Long) map.get("fagsakId"));

        Behandling behandling = builder.build();
        behandling.avsluttBehandling();
        lagreBehandling(behandling);

        List<Behandling> resultat = behandlingRepository.hentBehandlingerSomIkkeErAvsluttetForFagsakId(fagsakId);

        assertThat(resultat).isEmpty();
    }

    private Map<String, Object> opprettBuilderForBehandlingMedFagsakId() {
        Long fagsakId = fagsakRepository.lagre(fagsak);
        Behandling.Builder builder = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING);

        Map<String, Object> map = new HashMap<>();
        map.put("fagsakId", fagsakId);
        map.put("builder", builder);

        return map;
    }

    private Behandling.Builder opprettBuilderForBehandling() {
        fagsakRepository.lagre(fagsak);
        return Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING);

    }
}
