package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import no.nav.vedtak.exception.IntegrasjonException;

public class SperringKravgrunnlagException extends IntegrasjonException {
    public SperringKravgrunnlagException(String kode, String msg) {
        this(kode, msg, null);
    }

    public SperringKravgrunnlagException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}
