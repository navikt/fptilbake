package no.nav.foreldrepenger.tilbakekreving.grunnlag;

/**
 * Event publiserer når endret kravgrunnlag mottas
 */
public class SlettGrunnlagEvent {
    private long behandlingId;

    public SlettGrunnlagEvent(long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public long getBehandlingId() {
        return behandlingId;
    }
}
