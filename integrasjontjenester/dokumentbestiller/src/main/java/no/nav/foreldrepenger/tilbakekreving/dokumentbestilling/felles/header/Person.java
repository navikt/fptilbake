package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public class Person {
    @JsonProperty("navn")
    private String navn;
    @JsonProperty("fnr")
    private PersonIdent fnr;

    public Person(String navn, PersonIdent fnr) {
        this.navn = navn;
        this.fnr = fnr;
    }

    public String getNavn() {
        return navn;
    }

    public PersonIdent getFnr() {
        return fnr;
    }
}
