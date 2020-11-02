package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.batch;

public class GrunnlagStatus {

    private boolean skalGrunnlagSperres = false;

    public boolean erSkalGrunnlagSperres() {
        return skalGrunnlagSperres;
    }

    public void setSkalGrunnlagSperres(boolean skalGrunnlagSperres) {
        this.skalGrunnlagSperres = skalGrunnlagSperres;
    }
}
