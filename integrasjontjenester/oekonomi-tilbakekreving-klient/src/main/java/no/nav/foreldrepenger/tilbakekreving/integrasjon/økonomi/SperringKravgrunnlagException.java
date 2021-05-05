package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.vedtak.exception.IntegrasjonException;

public class SperringKravgrunnlagException extends IntegrasjonException {
    public SperringKravgrunnlagException(String kode, String msg) {
        this(kode, msg, (Throwable)null);
    }

    public SperringKravgrunnlagException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}
