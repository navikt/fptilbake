package no.nav.journalpostapi.dto.sak;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

public class Sak {

    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Sakstype sakstype;
    private String fagsakId;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private FagsakSystem fagsaksystem;
    private String arkivsaksnummer;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Arkivsaksystem arkivsaksystem;

    private Sak() {
    }

    public static class Builder {

        private Sak kladd = new Sak();

        public Builder medSakstype(Sakstype sakstype) {
            kladd.sakstype = sakstype;
            return this;
        }

        public Builder medFagsak(FagsakSystem fagsakSystem, String fagsakId) {
            kladd.fagsaksystem = fagsakSystem;
            kladd.fagsakId = fagsakId;
            return this;
        }

        public Builder medArkivsak(Arkivsaksystem arkivsaksystem, String arkivsaksnummer) {
            kladd.arkivsaksystem = arkivsaksystem;
            kladd.arkivsaksnummer = arkivsaksnummer;
            return this;
        }

        public Sak build() {
            Objects.requireNonNull(kladd.sakstype);
            switch (kladd.sakstype) {
                case FAGSAK:
                    Objects.requireNonNull(kladd.fagsakId, "Mangler fagsakId");
                    Objects.requireNonNull(kladd.fagsaksystem, "Mangler fagsaksystem");
                    requireNull(kladd.arkivsaksnummer, "Skal ikke bruke arkivsaksnummer sammen med FAGSAK");
                    requireNull(kladd.arkivsaksystem, "Skal ikke bruke arkivsaksystem sammen med FAGSAK");
                    break;
                case GENERELL_SAK:
                    requireNull(kladd.fagsakId, "Skal ikke bruke fagsakId sammen med GENERELL_SAK");
                    requireNull(kladd.fagsaksystem, "Skal ikke bruke fagsakystem sammen med GENERELL_SAK");
                    requireNull(kladd.arkivsaksnummer, "Skal ikke bruke arkivsaksnummer sammen med FAGSAK");
                    requireNull(kladd.arkivsaksystem, "Skal ikke bruke arkivsaksystem sammen med FAGSAK");
                    break;
                case ARKIVSAK:
                    requireNull(kladd.fagsakId, "Skal ikke bruke fagsakId sammen med ARKIVSAK");
                    requireNull(kladd.fagsaksystem, "Skal ikke bruke fagsakystem sammen med ARKIVSAK");
                    Objects.requireNonNull(kladd.arkivsaksnummer, "Mangler arkivsaksnummer");
                    Objects.requireNonNull(kladd.arkivsaksystem, "Mangler arkivsaksystem");
                    break;
            }
            return kladd;
        }
    }

    private static void requireNull(Object variable, String tekst) {
        if (variable != null) {
            throw new IllegalArgumentException(tekst);
        }
    }
}
