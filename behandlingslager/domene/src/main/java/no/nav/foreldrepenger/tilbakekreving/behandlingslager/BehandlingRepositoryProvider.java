package no.nav.foreldrepenger.tilbakekreving.behandlingslager;


import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

public interface BehandlingRepositoryProvider {

    KodeverkRepository getKodeverkRepository();

    AksjonspunktRepository getAksjonspunktRepository();

    BehandlingRepository getBehandlingRepository();

    BehandlingresultatRepository getBehandlingresultatRepository();

    FagsakRepository getFagsakRepository();

    HistorikkRepository getHistorikkRepository();

    BehandlingLåsRepository getBehandlingLåsRepository();

    FagsakLåsRepository getFagsakLåsRepository();

    KravgrunnlagRepository getGrunnlagRepository();

    FaktaFeilutbetalingRepository getFaktaFeilutbetalingRepository();

    EksternBehandlingRepository getEksternBehandlingRepository();

    VurdertForeldelseRepository getVurdertForeldelseRepository();

    VilkårsvurderingRepository getVilkårsvurderingRepository();

    BehandlingVedtakRepository getBehandlingVedtakRepository();

}
