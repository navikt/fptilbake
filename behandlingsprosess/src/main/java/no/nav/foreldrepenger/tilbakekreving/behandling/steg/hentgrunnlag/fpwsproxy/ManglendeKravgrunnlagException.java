package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import no.nav.vedtak.exception.IntegrasjonException;

public class ManglendeKravgrunnlagException extends IntegrasjonException {
    public ManglendeKravgrunnlagException(String kode, String msg) {
        this(kode, msg, null);
    }

    public ManglendeKravgrunnlagException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}
