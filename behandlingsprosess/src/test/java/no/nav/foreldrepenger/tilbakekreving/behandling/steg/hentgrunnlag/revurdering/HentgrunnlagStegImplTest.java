package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class HentgrunnlagStegImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private FagsakRepository fagsakRepository;

    private Fagsak fagsak;

    private Behandling behandling;

    @Before
    public void setup() {
        fagsak = TestFagsakUtil.opprettFagsak();
        fagsakRepository.lagre(fagsak);
        behandling = lagBehandling(null);
    }

    @Test
    public void utførSteg_medUtenAksjonspunkter() {
        BehandlingÅrsak.Builder behandlingÅrsakBuilder = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_KLAGE_KA).medOriginalBehandling(behandling);
        Behandling revurdering = lagBehandling(behandlingÅrsakBuilder);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(revurdering);

        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås));
        assertThat(stegResultat).isNotNull();
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(stegResultat.getAksjonspunktListe()).isEmpty();
    }

    private Behandling lagBehandling(BehandlingÅrsak.Builder builder) {
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.REVURDERING_TILBAKEKREVING)
            .medBehandlingÅrsak(builder).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Long behandlingId = behandlingRepository.lagre(behandling, lås);
        return behandlingRepository.hentBehandling(behandlingId);
    }


    private HentgrunnlagStegImpl steg() {
        return new HentgrunnlagStegImpl(prosessTaskRepository, behandlingRepository);
    }
}
