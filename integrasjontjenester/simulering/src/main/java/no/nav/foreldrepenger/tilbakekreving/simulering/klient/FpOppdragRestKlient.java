package no.nav.foreldrepenger.tilbakekreving.simulering.klient;

import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.SimuleringResultatDto;

import java.util.Optional;

public interface FpOppdragRestKlient {

    /**
     * Henter simuleringresultat for behandling hvis det finnes.
     *
     * @param behandlingId
     * @return Optional med SimuleringResultatDto kan være tom
     */
    Optional<SimuleringResultatDto> hentResultat(Long behandlingId);

    Optional<FeilutbetaltePerioderDto> hentFeilutbetaltePerioder(Long behandlingId);

}
