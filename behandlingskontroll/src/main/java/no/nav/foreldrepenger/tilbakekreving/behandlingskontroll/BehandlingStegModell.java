package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;


import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;

public interface BehandlingStegModell {

    /**
     * Type kode for dette steget.
     */
    BehandlingStegType getBehandlingStegType();

    /**
     * Implementasjon av et gitt steg i behandlingen.
     */
    BehandlingSteg getSteg();

    BehandlingModell getBehandlingModell();

}
