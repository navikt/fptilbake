package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import no.nav.vedtak.exception.IntegrasjonException;

public class UkjentKvitteringFraOSException extends IntegrasjonException {
    public UkjentKvitteringFraOSException(String kode, String msg) {
        this(kode, msg, null);
    }

    public UkjentKvitteringFraOSException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}
