package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;

@ExtendWith(JpaExtension.class)
class VarselresponsRepositoryTest {

    private static Long BEHANDLING_ID;
    private static final String KILDE = "MANU";

    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private VarselresponsRepository repository;

    @BeforeEach
    void setup(EntityManager entityManager) {
        behandlingRepository = new BehandlingRepository(entityManager);
        fagsakRepository = new FagsakRepository(entityManager);
        repository = new VarselresponsRepository(entityManager);

        BEHANDLING_ID = opprettBehandling();
    }

    private Long opprettBehandling() {
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        return behandlingRepository.lagre(behandling, lås);
    }

    @Test
    void test_skal_lagre_og_hente_respons_med_saksnummer() {
        Boolean grunnlagAkseptert = true;
        Varselrespons input = Varselrespons.builder()
                .medBehandlingId(BEHANDLING_ID)
                .setKilde(KILDE)
                .setAkseptertFaktagrunnlag(grunnlagAkseptert).build();

        repository.lagre(input);

        Optional<Varselrespons> lagret = repository.hentRespons(BEHANDLING_ID);

        assertThat(lagret).isNotEmpty();
        assertThat(lagret.get().getBehandlingId()).isEqualTo(BEHANDLING_ID);
        assertThat(lagret.get().getAkseptertFaktagrunnlag()).isEqualTo(grunnlagAkseptert);
        assertThat(lagret.get().getKilde()).isEqualTo(KILDE);
    }

    @Test
    void test_skal_oppdatere_respons_ikke_legge_til_ny_for_saksnummer() {
        Varselrespons input1 = Varselrespons.builder()
                .medBehandlingId(BEHANDLING_ID)
                .setKilde(KILDE)
                .setAkseptertFaktagrunnlag(false)
                .build();
        Varselrespons input2 = Varselrespons.builder()
                .medBehandlingId(BEHANDLING_ID)
                .setKilde(KILDE)
                .setAkseptertFaktagrunnlag(true)
                .build();

        repository.lagre(input1);
        repository.lagre(input2);

        Optional<Varselrespons> lagret = repository.hentRespons(BEHANDLING_ID);

        assertThat(lagret).isNotEmpty();
        assertThat(lagret.get().getBehandlingId()).isEqualTo(BEHANDLING_ID);
        assertThat(lagret.get().getAkseptertFaktagrunnlag()).isTrue();
        assertThat(lagret.get().getKilde()).isEqualTo(KILDE);
    }

    @Test
    void skal_returnere_tom_optional_når_saksnummer_ikke_finnes() {
        Optional<Varselrespons> resultat = repository.hentRespons(8383838L);

        assertThat(resultat).isEmpty();
    }

}
