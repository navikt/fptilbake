package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping;

import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.felles.RevurderingOpprettetÅrsak;

public class BehandlingÅrsakMapper {
    private static final Map<BehandlingÅrsakType, RevurderingOpprettetÅrsak> MAPPING = Map.of(
            BehandlingÅrsakType.RE_KLAGE_KA, RevurderingOpprettetÅrsak.KLAGE_KA,
            BehandlingÅrsakType.RE_KLAGE_NFP, RevurderingOpprettetÅrsak.KLAGE_NFP,
            BehandlingÅrsakType.RE_OPPLYSNINGER_OM_FORELDELSE, RevurderingOpprettetÅrsak.OPPLYSNINGER_OM_FORELDELSE,
            BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR, RevurderingOpprettetÅrsak.OPPLYSNINGER_OM_VILKÅR,
            BehandlingÅrsakType.RE_FEILUTBETALT_BELØP_HELT_ELLER_DELVIS_BORTFALT, RevurderingOpprettetÅrsak.FEILUTBETALING_BORTFALT);

    public static RevurderingOpprettetÅrsak getRevurderingÅrsak(BehandlingÅrsak behandlingÅrsak) {
        return getRevurderingÅrsak(behandlingÅrsak.getBehandlingÅrsakType());
    }

    public static RevurderingOpprettetÅrsak getRevurderingÅrsak(BehandlingÅrsakType behandlingÅrsakType) {
        var verdi = MAPPING.get(behandlingÅrsakType);
        if (verdi == null) {
            throw new IllegalArgumentException("Mangler mapping fra " + BehandlingÅrsakType.class + " til " + RevurderingOpprettetÅrsak.class + " for " + behandlingÅrsakType);
        }
        return verdi;
    }
}
