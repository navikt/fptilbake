package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

// Denne klassen har skrevet for å teste Behandling med steg og status
public class BehandlingStegMockUtil {

    private BehandlingStegMockUtil() {
        // for static access
    }

    public static void nyBehandlingSteg(Behandling behandling, BehandlingStegType behandlingStegType, BehandlingStatus behandlingStatus) {
        BehandlingStegTilstand behandlingStegTilstand = new BehandlingStegTilstand(behandling, behandlingStegType, BehandlingStegStatus.UTGANG);
        behandling.oppdaterBehandlingStegOgStatus(behandlingStegTilstand);
    }
}
