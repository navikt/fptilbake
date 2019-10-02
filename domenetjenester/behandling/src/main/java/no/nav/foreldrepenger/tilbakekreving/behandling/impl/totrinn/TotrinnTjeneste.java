package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnresultatgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAggregateEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseAggregate;

@ApplicationScoped
public class TotrinnTjeneste {

    private TotrinnRepository totrinnRepository;
    private BehandlingRepositoryProvider repositoryProvider;

    TotrinnTjeneste() {
        // for CDI
    }

    @Inject
    public TotrinnTjeneste(TotrinnRepository totrinnRepository, BehandlingRepositoryProvider repositoryProvider) {
        this.totrinnRepository = totrinnRepository;
        this.repositoryProvider = repositoryProvider;
    }

    public void settNyttTotrinnsgrunnlag(Behandling behandling) {
        Long behandlingId = behandling.getId();
        Optional<Long> faktaAggregateId = repositoryProvider.getFaktaFeilutbetalingRepository().finnFaktaFeilutbetalingAggregateId(behandlingId);
        Optional<VurdertForeldelseAggregate> foreldelseAggregate = repositoryProvider.getVurdertForeldelseRepository().finnVurdertForeldelseForBehandling(behandlingId);
        Optional<VilkårVurderingAggregateEntitet> vilkårVurderingAggregateEntitet = repositoryProvider.getVilkårsvurderingRepository().finnVilkårsvurderingForBehandlingId(behandlingId);

        Long feilUtbetalingId = faktaAggregateId.orElseThrow();
        Long foreldelseId = foreldelseAggregate.map(VurdertForeldelseAggregate::getId).orElse(null);
        Long vilkårId = vilkårVurderingAggregateEntitet.map(VilkårVurderingAggregateEntitet::getId).orElse(null);

        Totrinnresultatgrunnlag totrinnresultatgrunnlag = Totrinnresultatgrunnlag.builder()
            .medBehandling(behandling)
            .medFeilutbetalingId(feilUtbetalingId)
            .medForeldelseId(foreldelseId)
            .medVilkårId(vilkårId).build();
        totrinnRepository.lagreOgFlush(behandling, totrinnresultatgrunnlag);
    }

    public void settNyeTotrinnaksjonspunktvurderinger(Behandling behandling, List<Totrinnsvurdering> vurderinger) {
        totrinnRepository.lagreOgFlush(behandling, vurderinger);
    }

    public Collection<Totrinnsvurdering> hentTotrinnsvurderinger(Behandling behandling) {
        return totrinnRepository.hentTotrinnsvurderinger(behandling);
    }

    public Optional<Totrinnresultatgrunnlag> hentTotrinngrunnlagHvisEksisterer(Behandling behandling) {
        return totrinnRepository.hentTotrinngrunnlag(behandling);
    }
}
