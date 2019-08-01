package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

public interface BehandlingVenterRepository {

    Optional<Behandling> hentBehandlingPåVent(long behandlingId);

}
