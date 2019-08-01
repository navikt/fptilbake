package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Period;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.GrunnlagSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.MottattGrunnlagStegImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.TestFagsakUtil;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class MottattGrunnlagStegImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private KravgrunnlagRepository grunnlagRepository;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private Fagsak fagsak;

    @Before
    public void setup() {
        fagsak = TestFagsakUtil.opprettFagsak();
    }

    @Test
    public void skal_sette_behandling_på_vent() {
        fagsakRepository.lagre(fagsak);
        Behandling behandling = lagBehandling();

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        Aksjonspunkt ap = behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        assertThat(ap.getFristTid()).isEqualToIgnoringSeconds(LocalDateTime.now().plusWeeks(4));
    }

    @Test
    public void skal_utføre_steg_uten_aksjonspunkt() {
        fagsakRepository.lagre(fagsak);
        Behandling behandling = lagBehandling();
        KravgrunnlagAggregate kravgrunnlagAggregate = KravgrunnlagAggregate.builder().
                medBehandlingId(behandling.getId()).medAktiv(true).medGrunnlagØkonomi(KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList())).build();
        grunnlagRepository.lagre(kravgrunnlagAggregate);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(behandling.getAksjonspunkter()).isEmpty();
    }

    private Behandling lagBehandling() {
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Long behandlingId = behandlingRepository.lagre(behandling, lås);
        return behandlingRepository.hentBehandling(behandlingId);
    }

    private GrunnlagSteg steg() {
        return new MottattGrunnlagStegImpl(behandlingRepository, behandlingskontrollTjeneste, grunnlagRepository, Period.ofWeeks(4));
    }

}
