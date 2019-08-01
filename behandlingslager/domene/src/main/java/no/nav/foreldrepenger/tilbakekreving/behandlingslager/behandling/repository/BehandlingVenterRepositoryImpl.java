package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;

@ApplicationScoped
public class BehandlingVenterRepositoryImpl implements BehandlingVenterRepository {

    private FellesQueriesForBehandlingRepositories fellesQueriesForBehandlingRepositories;

    public BehandlingVenterRepositoryImpl() {
        // CDI
    }

    @Inject
    public BehandlingVenterRepositoryImpl(FellesQueriesForBehandlingRepositories fellesQueriesForBehandlingRepositories) {
        this.fellesQueriesForBehandlingRepositories = fellesQueriesForBehandlingRepositories;
    }

    @Override
    public Optional<Behandling> hentBehandlingPåVent(long behandlingId) {
        List<Behandling> behandlingerVenterTilbakemelding = fellesQueriesForBehandlingRepositories.finnVentendeBehandlingMedAktivtAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING);
        List<Behandling> behandlingerVenterGrunnlag = fellesQueriesForBehandlingRepositories.finnVentendeBehandlingMedAktivtAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        List<Behandling> behandlinger = new ArrayList<>();
        behandlinger.addAll(behandlingerVenterTilbakemelding);
        behandlinger.addAll(behandlingerVenterGrunnlag);

        return behandlinger.stream()
                .filter(o -> Objects.equals(behandlingId, o.getId()))
                .findFirst();
    }
}
