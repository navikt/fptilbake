package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.AutomatiskSaksbehandlingVurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

class MottattGrunnlagStegTest extends FellesTestOppsett {

    @Test
    void skal_sette_behandling_på_vent_dersom_mangler_grunnlag() {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        Aksjonspunkt ap = behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        assertThat(ap.getFristTid()).isEqualToIgnoringSeconds(LocalDateTime.now().plusWeeks(4));
    }

    @Test
    void skal_utføre_steg_uten_aksjonspunkt_dersom_feilutbetalt_under_halvt_gebyr_og_gammel() {
        lagKravgrunnlagMedFeilPostering(behandling.getId(), 100, LocalDateTime.now().minusWeeks(10));

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(behandling.getAksjonspunkter()).isEmpty();
    }

    @Test
    void skal_utføre_steg_og_vente_dersom_feilutbetalt_under_halvt_gebyr_og_nyere_dato() {
        lagKravgrunnlagMedFeilPostering(behandling.getId(), 100, LocalDateTime.now().minusWeeks(2));

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
        Aksjonspunkt ap = behandling.getAksjonspunktFor(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
        assertThat(ap.getFristTid()).isEqualToIgnoringSeconds(LocalDate.now()
            .minusWeeks(2).plus(Frister.KRAVGRUNNLAG_ALDER_GAMMELT).plusDays(1).atStartOfDay());
    }

    @Test
    void skal_utføre_steg_uten_aksjonspunkt_dersom_feilutbetalt_over_halvt_gebyr_og_nyere_dato() {
        lagKravgrunnlagMedFeilPostering(behandling.getId(), 10000, LocalDateTime.now().minusWeeks(10));

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));

        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandling.isBehandlingPåVent()).isFalse();
        assertThat(behandling.getAksjonspunkter()).isEmpty();
    }

    @Test
    void skal_fortsatt_på_vent_hvis_grunnlag_er_sperret() {
        grunnlagRepository.lagre(behandling.getId(), KravgrunnlagMockUtil.lagMockObject(new ArrayList<>()));
        grunnlagRepository.sperrGrunnlag(behandling.getId());

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandleStegResultat stegResultat = steg().gjenopptaSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås));
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        assertThat(behandling.isBehandlingPåVent()).isTrue();
    }

    private MottattGrunnlagSteg steg() {
        var gjenopptaBehandlingTjeneste = new GjenopptaBehandlingTjeneste(taskTjeneste, behandlingKandidaterRepository, behandlingVenterRepository,
            repositoryProvider);
        return new MottattGrunnlagSteg(behandlingRepository, behandlingskontrollTjeneste, gjenopptaBehandlingTjeneste,
            new AutomatiskSaksbehandlingVurderingTjeneste(grunnlagRepository, repositoryProvider.getVarselRepository()));
    }

    private void lagKravgrunnlagMedFeilPostering(long behandlingId, int beløp, LocalDateTime kontroll) {
        var fom = LocalDate.of(2020, 3, 10);
        var mockMedFeilPostering = new KravgrunnlagMock(fom, fom.withDayOfMonth(31), KlasseType.FEIL, BigDecimal.valueOf(beløp), BigDecimal.ZERO);
        var mockMedYtelPostering = new KravgrunnlagMock(fom, fom.withDayOfMonth(31), KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(beløp));
        mockMedYtelPostering.setKlasseKode(KlasseKode.FPADATAL);

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(List.of(mockMedFeilPostering, mockMedYtelPostering), kontroll);
        grunnlagRepository.lagre(behandlingId, kravgrunnlag431);
    }

}
