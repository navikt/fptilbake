package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingStatus;

public class BehandlingStatusMapper {

    private static final Map<no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus, BehandlingStatus> MAPPING = Map.of(
            no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus.OPPRETTET, BehandlingStatus.OPPRETTET,
            no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus.UTREDES, BehandlingStatus.UTREDES,
            no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus.FATTER_VEDTAK, BehandlingStatus.FATTER_VEDTAK,
            no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus.IVERKSETTER_VEDTAK, BehandlingStatus.IVERKSETTER_VEDTAK,
            no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus.AVSLUTTET, BehandlingStatus.AVSLUTTET);

    public static BehandlingStatus getBehandlingStatus(no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus behandlingStatus,
                                                       boolean venterPåBruker, boolean venterPåØkonomi) {
        if (venterPåBruker) {
            return BehandlingStatus.VENT_BRUKER;
        }
        if (venterPåØkonomi) {
            return BehandlingStatus.VENT_SAKSBEHANDLING;
        }
        var verdi = MAPPING.get(behandlingStatus);
        if (verdi == null) {
            throw new IllegalArgumentException("Mangler mapping fra " + no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus.class + " til " + BehandlingStatus.class + " for " + behandlingStatus);
        }
        return verdi;
    }

}
