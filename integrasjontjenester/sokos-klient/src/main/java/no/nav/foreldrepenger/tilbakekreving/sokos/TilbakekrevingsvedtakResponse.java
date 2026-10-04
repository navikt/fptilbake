package no.nav.foreldrepenger.tilbakekreving.sokos;

import java.time.LocalDate;

public record TilbakekrevingsvedtakResponse(Integer status,
                                            String melding,
                                            Integer vedtakId,
                                            LocalDate datoVedtakFagsystem) {
}
