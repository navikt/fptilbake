package no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingslagerRepository;

public interface SpråkKodeverkRepository extends BehandlingslagerRepository  {

    Optional<Språkkode> finnSpråkMedKodeverkEiersKode(String språkkode);
}
