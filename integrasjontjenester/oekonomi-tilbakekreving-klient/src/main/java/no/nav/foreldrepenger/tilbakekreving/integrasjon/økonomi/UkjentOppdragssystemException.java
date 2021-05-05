package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.vedtak.exception.IntegrasjonException;

public class UkjentOppdragssystemException extends IntegrasjonException {
    public UkjentOppdragssystemException(String kode, String msg) {
        this(kode, msg, (Throwable)null);
    }

    public UkjentOppdragssystemException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}
