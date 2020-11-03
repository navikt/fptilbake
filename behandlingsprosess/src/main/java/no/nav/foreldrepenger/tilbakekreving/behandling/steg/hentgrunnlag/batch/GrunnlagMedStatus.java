package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;

public class GrunnlagMedStatus {

    private Optional<Kravgrunnlag431> grunnlag;
    private boolean skalGrunnlagSperres = false;

    public GrunnlagMedStatus(Optional<Kravgrunnlag431> grunnlag) {
        this.grunnlag = grunnlag;
    }

    public GrunnlagMedStatus(Optional<Kravgrunnlag431> grunnlag, boolean skalGrunnlagSperres) {
        this.grunnlag = grunnlag;
        this.skalGrunnlagSperres = skalGrunnlagSperres;
    }

    public Optional<Kravgrunnlag431> getGrunnlag() {
        return grunnlag;
    }

    public Kravgrunnlag431 getEksaktGrunnlag() {
        return grunnlag.orElseThrow();
    }

    public boolean erSkalGrunnlagSperres() {
        return skalGrunnlagSperres;
    }

}
