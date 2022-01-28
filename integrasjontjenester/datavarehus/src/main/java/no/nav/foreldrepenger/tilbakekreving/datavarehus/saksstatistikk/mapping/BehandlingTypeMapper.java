package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.BehandlingType;

public class BehandlingTypeMapper {

    private static final Map<no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType, BehandlingType> MAPPING = Map.of(
            no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType.TILBAKEKREVING, BehandlingType.TILBAKEKREVING,
            no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType.REVURDERING_TILBAKEKREVING, BehandlingType.REVURDERING_TILBAKEKREVING
    );

    public static BehandlingType getBehandlingType(no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType behandlingType) {
        var verdi = MAPPING.get(behandlingType);
        if (verdi == null) {
            throw new IllegalArgumentException("Mangler mapping fra " + no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType.class + " til " + BehandlingType.class + " for " + behandlingType);
        }
        return verdi;
    }

}
