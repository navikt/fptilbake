package no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi;

import no.nav.vedtak.exception.IntegrasjonException;

public class ManglendeKravgrunnlagException extends IntegrasjonException {
    public ManglendeKravgrunnlagException(String kode, String msg) {
        this(kode, msg, (Throwable) null);
    }

    public ManglendeKravgrunnlagException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}
