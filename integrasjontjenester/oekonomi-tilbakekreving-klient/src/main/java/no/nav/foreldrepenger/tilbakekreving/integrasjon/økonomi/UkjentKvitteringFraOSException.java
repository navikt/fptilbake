package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.vedtak.exception.IntegrasjonException;

public class UkjentKvitteringFraOSException extends IntegrasjonException {
    public UkjentKvitteringFraOSException(String kode, String msg) {
        this(kode, msg, (Throwable) null);
    }

    public UkjentKvitteringFraOSException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}
