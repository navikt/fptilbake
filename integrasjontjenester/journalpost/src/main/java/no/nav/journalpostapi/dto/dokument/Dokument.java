package no.nav.journalpostapi.dto.dokument;

import java.util.ArrayList;
import java.util.List;

public class Dokument {
    private String tittel;
    private String brevkode;
    private Dokumentkategori dokumentkategori;
    private List<Dokumentvariant> dokumentvarianter = new ArrayList<>();

    private Dokument() {
    }

    public static class Builder {

        private Dokument kladd = new Dokument();

        public Builder medTittel(String tittel) {
            kladd.tittel = tittel;
            return this;
        }

        public Builder medBrevkode(String brevkode) {
            kladd.brevkode = brevkode;
            return this;
        }

        public Builder medDokumentkategori(Dokumentkategori dokumentkategori) {
            kladd.dokumentkategori = dokumentkategori;
            return this;
        }

        public Builder medDokumentvarianter(List<Dokumentvariant> dokumentvarianter) {
            kladd.dokumentvarianter.addAll(dokumentvarianter);
            return this;
        }

        public Builder medDokumentvariant(Dokumentvariant dokumentvarianter) {
            kladd.dokumentvarianter.add(dokumentvarianter);
            return this;
        }

        public Dokument build() {
            if (kladd.dokumentvarianter == null || kladd.dokumentvarianter.isEmpty()) {
                throw new IllegalArgumentException("Krever minst 1 dokumentvariant");
            }
            if (kladd.dokumentvarianter.stream().noneMatch(d -> Variantformat.Arkiv.equals(d.getVariantformat()))) {
                throw new IllegalArgumentException("Krever at det finnes variant av type " + Variantformat.Arkiv);
            }
            return kladd;
        }
    }
}
