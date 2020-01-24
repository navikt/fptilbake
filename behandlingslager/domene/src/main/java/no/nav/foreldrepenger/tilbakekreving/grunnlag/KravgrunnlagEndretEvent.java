package no.nav.foreldrepenger.tilbakekreving.grunnlag;

/**
 * Event publiserer når endret kravgrunnlag mottas
 */
public class KravgrunnlagEndretEvent {
    private long behandlingId;

    public KravgrunnlagEndretEvent(long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public long getBehandlingId() {
        return behandlingId;
    }
}
