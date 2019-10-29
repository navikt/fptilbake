package no.nav.foreldrepenger.tilbakekreving.simulering.klient;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;

public interface FpOppdragRestKlient {

    Optional<FeilutbetaltePerioderDto> hentFeilutbetaltePerioder(Long behandlingId);

}
