package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;

@ApplicationScoped
public class BehandlingVenterRepositoryImpl implements BehandlingVenterRepository {

    private FellesQueriesForBehandlingRepositories sharedQueries;

    public BehandlingVenterRepositoryImpl() {
        // CDI
    }

    @Inject
    public BehandlingVenterRepositoryImpl(FellesQueriesForBehandlingRepositories fellesQueriesForBehandlingRepositories) {
        this.sharedQueries = fellesQueriesForBehandlingRepositories;
    }

    @Override
    public Optional<Behandling> hentBehandlingPåVent(long behandlingId) {
        return sharedQueries.finnVentendeBehandlingMedAktivtAksjonspunkt(behandlingId, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
    }
}
