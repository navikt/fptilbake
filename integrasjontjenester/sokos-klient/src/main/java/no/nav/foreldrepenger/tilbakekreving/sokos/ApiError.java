package no.nav.foreldrepenger.tilbakekreving.sokos;

import java.time.OffsetDateTime;

public record ApiError(OffsetDateTime timestamp,
                       Integer status,
                       String error,
                       String message,
                       String path) {
}
