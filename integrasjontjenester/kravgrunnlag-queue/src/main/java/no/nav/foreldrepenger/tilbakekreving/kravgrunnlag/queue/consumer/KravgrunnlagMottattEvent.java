package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

public class KravgrunnlagMottattEvent {
    private String kravgrunnlagXml;

    KravgrunnlagMottattEvent(String kravgrunnlagXml) {
        this.kravgrunnlagXml = kravgrunnlagXml;
    }

    public String getKravgrunnlagXml() {
        return kravgrunnlagXml;
    }
}
