package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Period;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.GrunnlagSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;

public class MottattGrunnlagStegImplTest extends FellesTestOppsett {

    private BehandlingFristenUtløptEventPubliserer utløptEventPublisererMock = Mockito.mock(BehandlingFristenUtløptEventPubliserer.class);

    @Test
    public void skal_sette_behandling_på_vent() {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        Aksjonspunkt ap = behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        assertThat(ap.getFristTid()).isEqualToIgnoringSeconds(LocalDateTime.now().plusWeeks(4));
    }

    @Test
    public void skal_utføre_steg_uten_aksjonspunkt() {
        grunnlagRepository.lagre(behandling.getId(), KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList()));

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(behandling.getAksjonspunkter()).isEmpty();
    }

    @Test
    public void skal_fortsatt_på_vent_hvis_grunnlag_er_sperret() {
        grunnlagRepository.lagre(behandling.getId(), KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList()));
        grunnlagRepository.sperrGrunnlag(behandling.getId());

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().gjenopptaSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
    }

    private GrunnlagSteg steg() {
        return new MottattGrunnlagStegImpl(behandlingRepository, behandlingskontrollTjeneste, gjenopptaBehandlingTjeneste, utløptEventPublisererMock, Period.ofWeeks(4));
    }

}
