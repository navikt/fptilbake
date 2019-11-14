package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class VarselbrevSamletInfo {

    private String fritekstFraSaksbehandler;
    private List<Periode> feilutbetaltePerioder;
    private Long sumFeilutbetaling;
    private LocalDate fristdato;
    private BrevMetadata brevMetadata;
    private LocalDate revurderingVedtakDato;

    private VarselbrevSamletInfo() {

    }

    public BrevMetadata getBrevMetadata() {
        return brevMetadata;
    }

    public LocalDate getFristdato() {
        return fristdato;
    }

    public String getFritekstFraSaksbehandler() {
        return fritekstFraSaksbehandler;
    }

    public List<Periode> getFeilutbetaltePerioder() {
        return feilutbetaltePerioder;
    }

    public Long getSumFeilutbetaling() {
        return sumFeilutbetaling;
    }

    public LocalDate getRevurderingVedtakDato() {
        return revurderingVedtakDato;
    }

    public static class Builder {

        private VarselbrevSamletInfo varselbrev = new VarselbrevSamletInfo();

        public Builder medFritekstFraSaksbehandler(String fritekstFraSaksbehandler) {
            this.varselbrev.fritekstFraSaksbehandler = fritekstFraSaksbehandler;
            return this;
        }

        public Builder medSumFeilutbetaling(Long sumFeilutbetaling) {
            this.varselbrev.sumFeilutbetaling = sumFeilutbetaling;
            return this;
        }

        public Builder medFeilutbetaltePerioder(List<Periode> feilutbetaltePerioder) {
            this.varselbrev.feilutbetaltePerioder = feilutbetaltePerioder;
            return this;
        }

        public Builder medRevurderingVedtakDato(LocalDate revurderingVedtakDato) {
            this.varselbrev.revurderingVedtakDato = revurderingVedtakDato;
            return this;
        }

        public Builder medFristdato(LocalDate fristdato) {
            this.varselbrev.fristdato = fristdato;
            return this;
        }

        public Builder medMetadata(BrevMetadata metadata) {
            this.varselbrev.brevMetadata = metadata;
            return this;
        }

        public VarselbrevSamletInfo build() {
            return varselbrev;
        }

    }
}
