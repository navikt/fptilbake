package no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer;

public class XmlMottattEvent {
    private String mottattXml;

    XmlMottattEvent(String xml) {
        this.mottattXml = xml;
    }

    public String getMottattXml() {
        return mottattXml;
    }
}
