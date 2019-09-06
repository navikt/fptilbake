package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars.HbVedtaksbrevData;

public class VedtaksbrevData {

    private HbVedtaksbrevData vedtaksbrevData;
    private BrevMetadata metadata;

    public VedtaksbrevData(HbVedtaksbrevData vedtaksbrevData, BrevMetadata metadata) {
        this.vedtaksbrevData = vedtaksbrevData;
        this.metadata = metadata;
    }

    public HbVedtaksbrevData getVedtaksbrevData() {
        return vedtaksbrevData;
    }

    public BrevMetadata getMetadata() {
        return metadata;
    }
}
