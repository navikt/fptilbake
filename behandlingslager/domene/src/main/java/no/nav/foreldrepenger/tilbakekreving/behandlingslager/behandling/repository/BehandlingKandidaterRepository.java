package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

public interface BehandlingKandidaterRepository {

    List<Behandling> finnBehandlingerForAutomatiskGjenopptagelse();

}
