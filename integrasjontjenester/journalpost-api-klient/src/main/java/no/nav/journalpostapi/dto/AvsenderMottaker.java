package no.nav.journalpostapi.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvsenderMottaker {
    private String id;
    private SenderMottakerIdType idType;
    private String navn;

    private AvsenderMottaker() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public SenderMottakerIdType getIdType() {
        return idType;
    }

    public String getNavn() {
        return navn;
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

        public AvsenderMottaker build() {
            Objects.requireNonNull(kladd.id, "Mangler id");
            Objects.requireNonNull(kladd.idType, "Mangler idtype");
            return kladd;
        }
    }

}
