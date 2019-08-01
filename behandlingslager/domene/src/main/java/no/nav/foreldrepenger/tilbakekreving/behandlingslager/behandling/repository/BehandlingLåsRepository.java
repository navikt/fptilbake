package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingslagerRepository;

/**
 * @see BehandlingLås
 */
public interface BehandlingLåsRepository extends BehandlingslagerRepository {

    /** Initialiser lås og ta lock på tilhørende database rader. */
    BehandlingLås taLås(Long behandlingId);

    /**
     * Verifiser lås ved å sjekke mot underliggende lager.
     */
    void oppdaterLåsVersjon(BehandlingLås behandlingLås);


}
