package no.nav.foreldrepenger.tilbakekreving.pip;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;

@ExtendWith(JpaExtension.class)
public class PipRepositoryTest {

    private static final String SAKSBEHANDLER = "Z12345";

    private PipRepository pipRepository;

    private Behandling behandling;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        BehandlingRepository behandlingRepository = new BehandlingRepository(entityManager);
        pipRepository = new PipRepository(entityManager);

        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        new FagsakRepository(entityManager).lagre(fagsak);
        behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        behandling.setAnsvarligSaksbehandler(SAKSBEHANDLER);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
    }

    @Test
    public void skal_hentBehandlingData_medGyldigBehandlingId() {
        Optional<PipBehandlingData> pipBehandlingData = pipRepository.hentBehandlingData(behandling.getId());
        assertThat(pipBehandlingData).isPresent();
        PipBehandlingData behandlingData = pipBehandlingData.get();
        assertThat(behandlingData.getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(behandlingData.getStatusForBehandling()).isEqualTo(BehandlingStatus.OPPRETTET.getKode());
        assertThat(behandlingData.getFagsakstatus()).isEqualTo(FagsakStatus.UNDER_BEHANDLING.getKode());
    }

    @Test
    public void skal_hentBehandlingData_medGyldigBehandlingUuid() {
        Optional<PipBehandlingData> pipBehandlingData = pipRepository.hentBehandlingData(behandling.getUuid());
        assertThat(pipBehandlingData).isPresent();
        PipBehandlingData behandlingData = pipBehandlingData.get();
        assertThat(behandlingData.getBehandlingId()).isEqualTo(behandling.getId());
        assertThat(behandlingData.getStatusForBehandling()).isEqualTo(BehandlingStatus.OPPRETTET.getKode());
        assertThat(behandlingData.getFagsakstatus()).isEqualTo(FagsakStatus.UNDER_BEHANDLING.getKode());
    }


    @Test
    public void skal_hentBehandlingData_medUgyldigBehandlingId() {
        Optional<PipBehandlingData> pipBehandlingData = pipRepository.hentBehandlingData(1234l);
        assertThat(pipBehandlingData).isEmpty();
    }

}
