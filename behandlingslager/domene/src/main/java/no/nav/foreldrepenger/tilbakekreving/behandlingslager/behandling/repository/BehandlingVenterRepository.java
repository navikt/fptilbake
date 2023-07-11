package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;

@ApplicationScoped
public class BehandlingVenterRepository {

    private FellesQueriesForBehandlingRepositories sharedQueries;

    public BehandlingVenterRepository() {
        // CDI
    }

    @Inject
    public BehandlingVenterRepository(FellesQueriesForBehandlingRepositories fellesQueriesForBehandlingRepositories) {
        this.sharedQueries = fellesQueriesForBehandlingRepositories;
    }

    public Optional<Behandling> hentBehandlingPåVent(long behandlingId) {
        return sharedQueries.finnVentendeBehandlingMedAktivtAksjonspunkt(behandlingId, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
    }
}
