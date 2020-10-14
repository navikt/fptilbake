package no.nav.journalpostapi.dto.dokument;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dokument {
    private String brevkode;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Dokumentkategori dokumentKategori;
    private List<Dokumentvariant> dokumentvarianter = new ArrayList<>();
    private String tittel;

    private Dokument() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBrevkode() {
        return brevkode;
    }

    public Dokumentkategori getDokumentKategori() {
        return dokumentKategori;
    }

    public List<Dokumentvariant> getDokumentvarianter() {
        return dokumentvarianter;
    }

    public String getTittel() {
        return tittel;
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
            kladd.dokumentKategori = dokumentkategori;
            return this;
        }

        public Builder leggTilDokumentvariant(Dokumentvariant dokumentvarianter) {
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
