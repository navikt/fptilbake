package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilStrengMedNorskFormatSerialiserer;

public class HbPerson {

    @JsonProperty("navn")
    private String navn;

    @JsonProperty("dødsdato")
    @JsonSerialize(using = LocalDateTilStrengMedNorskFormatSerialiserer.class)
    private LocalDate dødsdato;

    @JsonProperty("er-gift")
    private boolean erGift;

    private HbPerson() {
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private HbPerson kladd = new HbPerson();

        public Builder medNavn(String navn) {
            kladd.navn = navn;
            return this;
        }

        public Builder medDødsdato(LocalDate dødsdato) {
            kladd.dødsdato = dødsdato;
            return this;
        }

        public Builder medErGift(boolean erGift) {
            kladd.erGift = erGift;
            return this;
        }

        public HbPerson build() {
            Objects.requireNonNull(kladd.navn, "navn");
            return kladd;
        }
    }
}
