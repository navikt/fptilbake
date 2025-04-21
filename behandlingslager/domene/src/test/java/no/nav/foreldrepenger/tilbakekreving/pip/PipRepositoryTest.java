package no.nav.foreldrepenger.tilbakekreving.pip;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;

@ExtendWith(JpaExtension.class)
class PipRepositoryTest {

    private static final String SAKSBEHANDLER = "Z12345";

    private PipRepository pipRepository;

    private Behandling behandling;

    @BeforeEach
    void setup(EntityManager entityManager) {
        BehandlingRepository behandlingRepository = new BehandlingRepository(entityManager);
        pipRepository = new PipRepository(entityManager);

        var fagsak = TestFagsakUtil.opprettFagsak();
        new FagsakRepository(entityManager).lagre(fagsak);
        behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        behandling.setAnsvarligSaksbehandler(SAKSBEHANDLER);
        var behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
    }

    @Test
    void skal_hentBehandlingData_medGyldigBehandlingId() {
        var pipBehandlingData = pipRepository.hentBehandlingData(behandling.getId());
        assertThat(pipBehandlingData).isPresent();
        var behandlingData = pipBehandlingData.get();
        assertThat(behandlingData.behandlingId()).isEqualTo(behandling.getId());
        assertThat(behandlingData.behandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(behandlingData.behandlingStatus()).isEqualTo(BehandlingStatus.OPPRETTET);
        assertThat(behandlingData.saksnummer()).isEqualTo(behandling.getSaksnummer());
        assertThat(behandlingData.aktørId()).isEqualTo(behandling.getAktørId());
        assertThat(behandlingData.ansvarligSaksbehandler()).isEqualTo(SAKSBEHANDLER);
    }

    @Test
    void skal_hentBehandlingData_medGyldigBehandlingUuid() {
        var pipBehandlingData = pipRepository.hentBehandlingData(behandling.getUuid());
        assertThat(pipBehandlingData).isPresent();
        var behandlingData = pipBehandlingData.get();
        assertThat(behandlingData.behandlingId()).isEqualTo(behandling.getId());
        assertThat(behandlingData.behandlingUuid()).isEqualTo(behandling.getUuid());
        assertThat(behandlingData.behandlingStatus()).isEqualTo(BehandlingStatus.OPPRETTET);
        assertThat(behandlingData.saksnummer()).isEqualTo(behandling.getSaksnummer());
        assertThat(behandlingData.aktørId()).isEqualTo(behandling.getAktørId());
        assertThat(behandlingData.ansvarligSaksbehandler()).isEqualTo(SAKSBEHANDLER);
    }

    @Test
    void audit_aktørId() {
        var eier = pipRepository.hentAktørIdSomEierFagsak(behandling.getSaksnummer().getVerdi());
        assertThat(eier).hasValueSatisfying(a -> assertThat(a).isEqualTo(behandling.getAktørId()));
    }


    @Test
    void skal_hentBehandlingData_medUgyldigBehandlingId() {
        var pipBehandlingData = pipRepository.hentBehandlingData(1234l);
        assertThat(pipBehandlingData).isEmpty();
    }

}
