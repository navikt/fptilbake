package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;

public interface BehandlingresultatRepository {

    Long lagre(Behandlingsresultat behandlingsresultat);

    Optional<Behandlingsresultat> hent(Behandling behandling);

}
