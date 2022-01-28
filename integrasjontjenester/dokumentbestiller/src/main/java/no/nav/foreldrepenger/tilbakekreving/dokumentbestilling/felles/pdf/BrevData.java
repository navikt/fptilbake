package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;

public class BrevData {
    private BrevMetadata metadata;
    private String tittel;
    private String overskrift;
    private BrevMottaker mottaker;
    private String brevtekst;
    private String vedleggHtml;

    private BrevData() {
    }

    public BrevMetadata getMetadata() {
        return metadata;
    }

    public String getTittel() {
        return tittel;
    }

    public String getOverskrift() {
        return overskrift;
    }

    public BrevMottaker getMottaker() {
        return mottaker;
    }

    public String getBrevtekst() {
        return brevtekst;
    }

    public String getVedleggHtml() {
        return vedleggHtml;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BrevData brevData = (BrevData) o;
        return metadata.equals(brevData.metadata) &&
                Objects.equals(tittel, brevData.tittel) &&
                overskrift.equals(brevData.overskrift) &&
                mottaker == brevData.mottaker &&
                brevtekst.equals(brevData.brevtekst) &&
                Objects.equals(vedleggHtml, brevData.vedleggHtml);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, overskrift, tittel, mottaker, brevtekst, vedleggHtml);
    }

    public static class Builder {
        private BrevMetadata metadata;
        private String tittel;
        private String overskrift;
        private BrevMottaker mottaker;
        private String brevtekst;
        private String vedleggHtml;

        private Builder() {
        }

        public Builder setMetadata(BrevMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder setOverskrift(String overskrift) {
            this.overskrift = overskrift;
            return this;
        }

        public Builder setTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

        public Builder setMottaker(BrevMottaker mottaker) {
            this.mottaker = mottaker;
            return this;
        }

        public Builder setBrevtekst(String brevtekst) {
            this.brevtekst = brevtekst;
            return this;
        }

        public Builder setVedleggHtml(String vedleggHtml) {
            this.vedleggHtml = vedleggHtml;
            return this;
        }

        public BrevData build() {
            Objects.requireNonNull(metadata, "metadata må settes");
            Objects.requireNonNull(overskrift, "overskrift må settes");
            Objects.requireNonNull(mottaker, "mottaker må settes");
            Objects.requireNonNull(brevtekst, "brevtekst må settes");

            BrevData data = new BrevData();
            data.metadata = metadata;
            data.tittel = tittel;
            data.overskrift = overskrift;
            data.mottaker = mottaker;
            data.brevtekst = brevtekst;
            data.vedleggHtml = vedleggHtml != null ? vedleggHtml : "";
            return data;
        }
    }

}
