package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.HandlebarsData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;

public class HbVedtaksbrevData implements HandlebarsData {
    @JsonUnwrapped
    private HbVedtaksbrevFelles felles;

    @JsonProperty("antall-perioder")
    private int antallPerioder;

    @JsonProperty("perioder")
    private List<HbVedtaksbrevPeriode> perioder;

    public HbVedtaksbrevData(HbVedtaksbrevFelles felle, List<HbVedtaksbrevPeriode> perioder) {
        this.felles = felle;
        this.perioder = perioder;
        this.antallPerioder = perioder.size();
    }

    public HbVedtaksbrevFelles getFelles() {
        return felles;
    }

    public List<HbVedtaksbrevPeriode> getPerioder() {
        return perioder;
    }

    @Override
    public Språkkode getSpråkkode() {
        return felles.getSpråkkode();
    }
}
