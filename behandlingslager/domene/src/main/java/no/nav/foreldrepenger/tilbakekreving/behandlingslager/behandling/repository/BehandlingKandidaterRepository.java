package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;

@ApplicationScoped
public class BehandlingKandidaterRepository {

    private FellesQueriesForBehandlingRepositories sharedQueries;

    BehandlingKandidaterRepository() {
        // CDI
    }

    @Inject
    public BehandlingKandidaterRepository(FellesQueriesForBehandlingRepositories sharedQueries) {
        this.sharedQueries = sharedQueries;
    }

    public Set<Behandling> finnBehandlingerForAutomatiskGjenopptagelse() {
        LocalDate iDag = LocalDate.now();

        Collection<Behandling> ventendeBehandlinger = sharedQueries.finnVentendeBehandlingerMedAktivtAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        return ventendeBehandlinger.stream()
                .filter(o -> iDag.isAfter(o.getFristDatoBehandlingPåVent()))
                .collect(Collectors.toSet());
    }
}
