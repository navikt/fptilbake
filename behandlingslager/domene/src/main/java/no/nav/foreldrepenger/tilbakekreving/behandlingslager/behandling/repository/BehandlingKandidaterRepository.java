package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class BehandlingKandidaterRepository {

    private FellesQueriesForBehandlingRepositories sharedQueries;

    public BehandlingKandidaterRepository() {
        // CDI
    }

    @Inject
    public BehandlingKandidaterRepository(FellesQueriesForBehandlingRepositories sharedQueries) {
        this.sharedQueries = sharedQueries;
    }

    public Set<Behandling> finnBehandlingerForAutomatiskGjenopptagelse() {
        LocalDate iDag = FPDateUtil.iDag();

        Collection<Behandling> ventendeBehandlinger = sharedQueries.finnVentendeBehandlingerMedAktivtAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        return ventendeBehandlinger.stream()
            .filter(o -> iDag.isAfter(o.getFristDatoBehandlingPåVent()))
            .collect(Collectors.toSet());
    }
}
