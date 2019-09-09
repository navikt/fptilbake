package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;

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
