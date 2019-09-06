package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.BrevMetadata;

public class FritekstbrevData {

    private String overskrift;
    private String brevtekst;
    private BrevMetadata brevMetadata;

    public String getOverskrift() {
        return overskrift;
    }

    public String getBrevtekst() {
        return brevtekst;
    }

    public BrevMetadata getBrevMetadata() {
        return brevMetadata;
    }

    private FritekstbrevData() {
    }

    static class Builder {
        private FritekstbrevData kladd = new FritekstbrevData();

        public Builder medOverskrift(String overskrift) {
            kladd.overskrift = overskrift;
            return this;
        }

        public Builder medBrevtekst(String brevtekst) {
            kladd.brevtekst = brevtekst;
            return this;
        }

        public Builder medMetadata(BrevMetadata brevMetadata) {
            kladd.brevMetadata = brevMetadata;
            return this;
        }

        public FritekstbrevData build() {
            Objects.requireNonNull(kladd.overskrift, "overskrift mangler");
            Objects.requireNonNull(kladd.brevtekst, "brevtekst mangler");
            Objects.requireNonNull(kladd.brevMetadata, "metadata mangler");
            return kladd;
        }
    }

}
