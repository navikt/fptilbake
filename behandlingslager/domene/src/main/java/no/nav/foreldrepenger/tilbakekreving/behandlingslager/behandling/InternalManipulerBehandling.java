package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;


@ApplicationScoped
public class InternalManipulerBehandling {
    private KodeverkRepository kodeverkRepository;
    private BehandlingRepository behandlingRepository;

    public InternalManipulerBehandling() {
        // For CDI proxy
    }

    @Inject
    public InternalManipulerBehandling(BehandlingRepositoryProvider repositoryProvider) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    /** Sett til angitt steg, default steg status, default slutt status for andre åpne steg. */
    public void forceOppdaterBehandlingSteg(Behandling behandling, BehandlingStegType stegType) {
        forceOppdaterBehandlingSteg(behandling, stegType, BehandlingStegStatus.UDEFINERT);
    }

    /** Sett Behandling til angitt steg, angitt steg status, defalt slutt status for andre åpne steg. */
    public void forceOppdaterBehandlingSteg(Behandling behandling, BehandlingStegType stegType, BehandlingStegStatus stegStatus) {
        forceOppdaterBehandlingSteg(behandling, stegType, stegStatus, BehandlingStegStatus.UTFØRT);
    }

    /** Sett Behandling til angitt steg, angitt steg status, angitt slutt status for andre åpne steg. */
    public void forceOppdaterBehandlingSteg(Behandling behandling, BehandlingStegType stegType, BehandlingStegStatus nesteStegStatus,
                                            BehandlingStegStatus sluttStatusForEksisterendeSteg) {

        // finn riktig mapping av kodeverk slik at vi får med dette når Behandling brukes videre.
        BehandlingStegStatus nesteStatus = canonicalize(nesteStegStatus);
        BehandlingStegStatus sluttStatus = canonicalize(
                (sluttStatusForEksisterendeSteg == null ? BehandlingStegStatus.UTFØRT : sluttStatusForEksisterendeSteg));
        BehandlingStegType canonStegType = canonicalize(stegType);

        // Oppdater behandling til den nye stegtilstanden
        BehandlingStegTilstand stegTilstand = new BehandlingStegTilstand(behandling, canonStegType);
        stegTilstand.setBehandlingStegStatus(nesteStatus);
        behandling.oppdaterBehandlingStegOgStatus(stegTilstand, sluttStatus);
    }

    private BehandlingStegType canonicalize(BehandlingStegType stegType) {
        return stegType == null ? null : behandlingRepository.finnBehandlingStegType(stegType.getKode());
    }

    @SuppressWarnings("unchecked")
    protected <V extends Kodeliste> V canonicalize(V kodeliste) {
        return kodeliste == null ? null
                : (V) kodeverkRepository.finn(kodeliste.getClass(), kodeliste.getKode());
    }

}
