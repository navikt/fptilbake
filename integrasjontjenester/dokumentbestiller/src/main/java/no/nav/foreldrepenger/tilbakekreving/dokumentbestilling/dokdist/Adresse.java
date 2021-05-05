package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dokdist;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

public class Adresse {

    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    @JsonProperty("adressetype")
    private AdresseType adresseType;
    @JsonProperty("adresselinje1")
    private String adresselinje1;
    @JsonProperty("adresselinje2")
    private String adresselinje2;
    @JsonProperty("adresselinje3")
    private String adresselinje3;
    @JsonProperty("postnummer")
    private String postnummer;
    @JsonProperty("poststed")
    private String poststed;
    @JsonProperty("land")
    private String land;

    public AdresseType getAdresseType() {
        return adresseType;
    }

    public String getAdresselinje1() {
        return adresselinje1;
    }

    public String getAdresselinje2() {
        return adresselinje2;
    }

    public String getAdresselinje3() {
        return adresselinje3;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public String getLand() {
        return land;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AdresseType adresseType;
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String postnummer;
        private String poststed;
        private String land;

        private Builder() {
        }

        public Builder medAdresseType(AdresseType adresseType) {
            this.adresseType = adresseType;
            return this;
        }

        public Builder medAdresselinje1(String adresselinje1) {
            this.adresselinje1 = adresselinje1;
            return this;
        }

        public Builder medAdresselinje2(String adresselinje2) {
            this.adresselinje2 = adresselinje2;
            return this;
        }

        public Builder medAdresselinje3(String adresselinje3) {
            this.adresselinje3 = adresselinje3;
            return this;
        }

        public Builder medPoststed(String poststed) {
            this.poststed = poststed;
            return this;
        }

        public Builder medPostnummer(String postnummer) {
            this.postnummer = postnummer;
            return this;
        }

        public Builder medLand(String land) {
            this.land = land;
            return this;
        }

        public Adresse build() {
            Objects.requireNonNull(adresseType, "Adressetype må være satt");
            Objects.requireNonNull(land, "Land må være satt");
            if (land.length() != 2) {
                throw new IllegalArgumentException("Land må være To-bokstavers landkode ihht iso3166-1 alfa-2, men var " + land.length() + " tegn");
            }
            switch (adresseType) {
                case NORSK -> {
                    Objects.requireNonNull(postnummer, "Postnummer må være satt");
                    Objects.requireNonNull(poststed, "Poststed må være satt");
                }
                case UTENLANDSK -> Objects.requireNonNull(adresselinje1, "Adresselinje1 må være satt");
                default -> throw new IllegalArgumentException("Ikke-støttet adressetype: " + adresseType);
            }

            Adresse adresse = new Adresse();
            adresse.adresseType = adresseType;
            adresse.adresselinje1 = adresselinje1;
            adresse.adresselinje2 = adresselinje2;
            adresse.adresselinje3 = adresselinje3;
            adresse.postnummer = postnummer;
            adresse.poststed = poststed;
            adresse.land = land;
            return adresse;
        }
    }
}
