package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class HbVedtaksbrevData implements HandlebarsData {
    @JsonUnwrapped
    private HbVedtaksbrevFelles felles;

    @JsonProperty("perioder")
    private List<HbVedtaksbrevPeriode> perioder;

    public HbVedtaksbrevData(HbVedtaksbrevFelles felle, List<HbVedtaksbrevPeriode> perioder) {
        this.felles = felle;
        this.perioder = perioder;
    }

    public HbVedtaksbrevFelles getFelles() {
        return felles;
    }

    public List<HbVedtaksbrevPeriode> getPerioder() {
        return perioder;
    }
}
