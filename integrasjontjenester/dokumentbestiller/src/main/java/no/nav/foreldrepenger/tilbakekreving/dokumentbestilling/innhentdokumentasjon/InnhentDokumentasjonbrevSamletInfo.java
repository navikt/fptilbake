package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import java.time.LocalDate;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;

class InnhentDokumentasjonbrevSamletInfo {

    private String fritekstFraSaksbehandler;
    private LocalDate fristDato;
    private BrevMetadata brevMetadata;

    private InnhentDokumentasjonbrevSamletInfo() {
    }

    public BrevMetadata getBrevMetadata() {
        return brevMetadata;
    }

    public String getFritekstFraSaksbehandler() {
        return fritekstFraSaksbehandler;
    }

    public LocalDate getFristDato() {
        return fristDato;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final InnhentDokumentasjonbrevSamletInfo kladd;

        private Builder() {
            kladd = new InnhentDokumentasjonbrevSamletInfo();
        }

        public Builder medBrevMetaData(BrevMetadata brevMetadata) {
            kladd.brevMetadata = brevMetadata;
            return this;
        }

        public Builder medFritekstFraSaksbehandler(String fritekst) {
            kladd.fritekstFraSaksbehandler = fritekst;
            return this;
        }

        public Builder medFristDato(LocalDate fristDato) {
            kladd.fristDato = fristDato;
            return this;
        }

        public InnhentDokumentasjonbrevSamletInfo build() {
            return kladd;
        }
    }
}
