package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.AsyncPollingStatus;

public interface BehandlingsprosessApplikasjonTjeneste {

    Behandling hentBehandling(Long behandlingId);

    /** Sjekker om det pågår åpne prosess tasks (for angitt gruppe). Returnerer eventuelt task gruppe for eventuell åpen prosess task gruppe. */
    Optional<AsyncPollingStatus> sjekkProsessTaskPågårForBehandling(Behandling behandling, String gruppe);

}
