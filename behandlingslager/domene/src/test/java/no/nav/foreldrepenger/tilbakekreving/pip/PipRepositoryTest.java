package no.nav.foreldrepenger.tilbakekreving.pip;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Sets;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.test.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;

public class PipRepositoryTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private static final String SAKSBEHANDLER="Z12345";

    private PipRepository pipRepository = new PipRepository(repositoryRule.getEntityManager());

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(repositoryRule.getEntityManager());
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private Behandling behandling;

    @Before
    public void setup(){
        Fagsak fagsak = TestFagsakUtil.opprettFagsak();
        repositoryProvider.getFagsakRepository().lagre(fagsak);
        behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        behandling.setAnsvarligSaksbehandler(SAKSBEHANDLER);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling,behandlingLås);
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

    @Test
    public void skal_hentAksjonspunkttypeForAksjonspunktkoder_medGyldigAkskonspunktKode(){
        Set<String> aksjonspunktTyper = pipRepository.hentAksjonspunkttypeForAksjonspunktkoder(Sets.newHashSet("5001","7001"));
        assertThat(aksjonspunktTyper).isNotEmpty();
        assertThat(aksjonspunktTyper).contains("Manuell");
        assertThat(aksjonspunktTyper).contains("Auto");
    }

    @Test
    public void skal_hentAksjonspunkttypeForAksjonspunktkoder_medTomAkskonspunktKode(){
        Set<String> aksjonspunktTyper = pipRepository.hentAksjonspunkttypeForAksjonspunktkoder(Sets.newHashSet());
        assertThat(aksjonspunktTyper).isEmpty();
    }
}
