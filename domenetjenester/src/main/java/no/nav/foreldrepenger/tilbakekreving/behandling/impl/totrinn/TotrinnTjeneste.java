package no.nav.foreldrepenger.tilbakekreving.behandling.impl.totrinn;

import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagEndretEvent;

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

    public void settNyeTotrinnaksjonspunktvurderinger(Behandling behandling, List<Totrinnsvurdering> vurderinger) {
        totrinnRepository.lagreOgFlush(behandling, vurderinger);
    }

    public Collection<Totrinnsvurdering> hentTotrinnsvurderinger(Behandling behandling) {
        return totrinnRepository.hentTotrinnsvurderinger(behandling);
    }

    public void slettGammelTotrinnData(@Observes KravgrunnlagEndretEvent event) {
        totrinnRepository.slettGammelTotrinnData(event.getBehandlingId());
    }
}
