package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.HelgHarYtelsedager;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

@ApplicationScoped
public class TilbakekrevingBeregningTjeneste {

    private KravgrunnlagRepository kravgrunnlagRepository;
    private VurdertForeldelseRepository vurdertForeldelseRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BehandlingRepository behandlingRepository;

    TilbakekrevingBeregningTjeneste() {
        //for CDI proxy
    }

    @Inject
    public TilbakekrevingBeregningTjeneste(BehandlingRepositoryProvider repositoryProvider) {
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.vilkårsvurderingRepository = repositoryProvider.getVilkårsvurderingRepository();
        this.vurdertForeldelseRepository = repositoryProvider.getVurdertForeldelseRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    public BeregningResultat beregn(Long behandlingId) {
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VurdertForeldelse vurdertForeldelse = hentVurdertForeldelse(behandlingId);
        VilkårVurderingEntitet vilkårsvurdering = hentVilkårsvurdering(behandlingId);

        FagsakYtelseType fagsakYtelseType = behandling.getFagsak().getFagsakYtelseType();
        boolean helgHarYtelsedager = HelgHarYtelsedager.helgHarYtelsedager(fagsakYtelseType);
        boolean kanBeregneRenter = fagsakYtelseType != FagsakYtelseType.FRISINN;

        TilbakekrevingBeregner beregner = new TilbakekrevingBeregner(helgHarYtelsedager, kanBeregneRenter);
        return beregner.beregn(kravgrunnlag, vurdertForeldelse, vilkårsvurdering);
    }

    private VilkårVurderingEntitet hentVilkårsvurdering(Long behandlingId) {
        VilkårVurderingEntitet vurderingUtenPerioder = new VilkårVurderingEntitet();
        return vilkårsvurderingRepository.finnVilkårsvurdering(behandlingId)
            .orElse(vurderingUtenPerioder);
    }

    private VurdertForeldelse hentVurdertForeldelse(Long behandlingId) {
        VurdertForeldelse vurderingUtenPerioder = new VurdertForeldelse();
        return vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId).orElse(vurderingUtenPerioder);
    }

}
