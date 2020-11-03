package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;

public class KravgrunnlagMedStatus {

    private Kravgrunnlag431 kravgrunnlag;
    private boolean sperret;

    public static KravgrunnlagMedStatus utenGrunnlag() {
        return new KravgrunnlagMedStatus(null, false);
    }

    public static KravgrunnlagMedStatus forIkkeSperretKravgrunnlag(Kravgrunnlag431 kravgrunnlag) {
        return new KravgrunnlagMedStatus(kravgrunnlag, false);
    }

    public static KravgrunnlagMedStatus forSperretKravgrunnlag(Kravgrunnlag431 kravgrunnlag) {
        return new KravgrunnlagMedStatus(kravgrunnlag, true);
    }

    private KravgrunnlagMedStatus(Kravgrunnlag431 kravgrunnlag, boolean sperret) {
        this.kravgrunnlag = kravgrunnlag;
        this.sperret = sperret;
    }

    public boolean harKravgrunnlag() {
        return kravgrunnlag != null;
    }

    public Kravgrunnlag431 getKravgrunnlag() {
        validerHarKravgrunnlag();
        return kravgrunnlag;
    }

    public boolean erKravgrunnlagSperret() {
        validerHarKravgrunnlag();
        return sperret;
    }

    private void validerHarKravgrunnlag() {
        if (!harKravgrunnlag()) {
            throw new IllegalArgumentException("Har ikke kravgrunnlag");
        }
    }

}
