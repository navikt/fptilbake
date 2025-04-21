package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto;

import java.util.function.Function;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.TilbakekrevingAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class BehandlingReferanseAbacAttributter {

    public static AbacDataAttributter fraBehandlingReferanse(BehandlingReferanse ref) {
        if (ref.getBehandlingId() != null) {
            return AbacDataAttributter.opprett().leggTil(TilbakekrevingAbacAttributtType.BEHANDLING_ID, ref.getBehandlingId());
        } else {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, ref.getBehandlingUuid());
        }
    }

    public static class AbacDataBehandlingReferanse implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (BehandlingReferanse) obj;
            return fraBehandlingReferanse(req);
        }
    }
}
