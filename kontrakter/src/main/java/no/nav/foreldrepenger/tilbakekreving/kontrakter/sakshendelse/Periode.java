package no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Periode {

    @JsonProperty(value = "fom")
    private LocalDate fom;

    @JsonProperty(value = "tom")
    private LocalDate tom;

    public Periode(LocalDate fom, LocalDate tom) {
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }
}
