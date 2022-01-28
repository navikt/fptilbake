package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import no.nav.vedtak.exception.TekniskException;

public class BehandlingRepositoryFeil {


    public static TekniskException fantIkkeEntitetForLåsing(String entityClassName, long id) {
        return new TekniskException("FPT-131239", String.format("Fant ikke entitet for låsing [%s], id=%s.", entityClassName, id));
    }

    public static TekniskException fantIkkeBehandlingVedtak(long behandlingId) {
        return new TekniskException("FPT-131240", String.format("Fant ikke BehandlingVedtak, behandlingId=%s.", behandlingId));
    }

}
