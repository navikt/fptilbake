package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakEvent;

/**
 * Marker interface for events fyrt på en Behandling.
 * Disse fyres ved hjelp av CDI Events.
 */
public interface BehandlingEvent extends FagsakEvent {

    Long getBehandlingId();

}
