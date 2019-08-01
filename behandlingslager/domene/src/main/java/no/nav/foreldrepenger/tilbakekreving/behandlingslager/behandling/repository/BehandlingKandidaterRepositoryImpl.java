package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class BehandlingKandidaterRepositoryImpl implements BehandlingKandidaterRepository {

    private FellesQueriesForBehandlingRepositories sharedQueries;

    public BehandlingKandidaterRepositoryImpl() {
        // CDI
    }

    @Inject
    public BehandlingKandidaterRepositoryImpl(FellesQueriesForBehandlingRepositories sharedQueries) {
        this.sharedQueries = sharedQueries;
    }

    @Override
    public List<Behandling> finnBehandlingerForAutomatiskGjenopptagelse() {
        LocalDate iDag = FPDateUtil.iDag();

        List<Behandling> behandlinger = new ArrayList<>();
        leggTilBehandlingerSomVenterPåBrukerTilbakemelding(behandlinger);
        leggTilBehandlingerSomVenterPåTilbakekrevingsGrunnlag(behandlinger);

        return behandlinger.stream()
                .filter(o -> iDag.isAfter(o.getFristDatoBehandlingPåVent()))
                .collect(Collectors.toList());
    }

    private void leggTilBehandlingerSomVenterPåBrukerTilbakemelding(List<Behandling> behandlinger) {
        behandlinger.addAll(sharedQueries.finnVentendeBehandlingMedAktivtAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING));
    }

    private void leggTilBehandlingerSomVenterPåTilbakekrevingsGrunnlag(List<Behandling> behandlinger) {
        behandlinger.addAll(sharedQueries.finnVentendeBehandlingMedAktivtAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG));
    }

}
