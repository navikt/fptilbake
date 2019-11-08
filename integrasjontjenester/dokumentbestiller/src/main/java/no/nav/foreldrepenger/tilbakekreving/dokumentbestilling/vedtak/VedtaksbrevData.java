package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;

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

    public VedtakResultatType getHovedresultat() {
        return vedtaksbrevData.getFelles().getHovedresultat();
    }
}
