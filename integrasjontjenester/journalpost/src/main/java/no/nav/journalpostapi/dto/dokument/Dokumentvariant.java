package no.nav.journalpostapi.dto.dokument;

import java.util.Base64;
import java.util.Objects;

public class Dokumentvariant {
    private Filtype filtype;
    private Variantformat variantformat;
    private String fysiskDokument;
    private String filnavn;

    public Variantformat getVariantformat() {
        return variantformat;
    }

    private Dokumentvariant() {
    }

    public static class Builder {

        private Dokumentvariant kladd = new Dokumentvariant();
        private byte[] dokumentInnhold;

        public Builder medDokument(byte[] dokumentInnhold) {
            this.dokumentInnhold = dokumentInnhold;
            return this;
        }

        public Builder medFiltype(Filtype filtype) {
            kladd.filtype = filtype;
            return this;
        }

        public Builder medVariantformat(Variantformat variantformat) {
            kladd.variantformat = variantformat;
            return this;
        }


        public Builder medFilnavn(String filnavn) {
            kladd.filnavn = filnavn;
            return this;
        }

        public Dokumentvariant build() {
            Objects.requireNonNull(dokumentInnhold, "mangler dokumentinnhold");
            Objects.requireNonNull(kladd.filtype, "mangler filtype");
            Objects.requireNonNull(kladd.variantformat, "mangler variantformat");
            kladd.fysiskDokument = Base64.getEncoder().encodeToString(dokumentInnhold);
            return kladd;
        }
    }
}
