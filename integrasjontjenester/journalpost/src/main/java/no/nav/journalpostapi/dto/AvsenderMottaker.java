package no.nav.journalpostapi.dto;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

public class AvsenderMottaker {

    private String id;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private SenderMottakerIdType idType;
    private String navn;
    private String land;

    private AvsenderMottaker() {
    }

    public static class Builder {

        private AvsenderMottaker kladd = new AvsenderMottaker();

        public Builder medId(SenderMottakerIdType idType, String id) {
            kladd.idType = idType;
            kladd.id = id;
            return this;
        }

        public Builder medNavn(String navn) {
            kladd.navn = navn;
            return this;
        }

        public Builder medLand(String land) {
            kladd.land = land;
            return this;
        }

        public AvsenderMottaker build() {
            Objects.requireNonNull(kladd.id, "Mangler id");
            Objects.requireNonNull(kladd.idType, "Mangler idtype");
            return kladd;
        }
    }

}
