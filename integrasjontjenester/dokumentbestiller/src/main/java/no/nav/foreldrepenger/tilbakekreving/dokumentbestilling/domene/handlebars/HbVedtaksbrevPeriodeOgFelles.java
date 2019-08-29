package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class HbVedtaksbrevPeriodeOgFelles implements HandlebarsData {

    @JsonUnwrapped
    private HbVedtaksbrevFelles felles;

    @JsonUnwrapped
    private HbVedtaksbrevPeriode periode;

    public HbVedtaksbrevPeriodeOgFelles(HbVedtaksbrevFelles felles, HbVedtaksbrevPeriode periode) {
        this.felles = felles;
        this.periode = periode;
    }

    public HbVedtaksbrevPeriode getPeriode() {
        return periode;
    }
}
