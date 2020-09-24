package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilLangtNorskFormatSerialiserer;

public class Brev {

    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate dato;

    private String overskrift;

    public Brev(String overskrift) {
        this.overskrift = overskrift;
        this.dato = LocalDate.now();
    }

    public void setDato(LocalDate dato) {
        this.dato = dato;
    }

    public String getOverskrift() {
        return overskrift;
    }

    public LocalDate getDato() {
        return dato;
    }
}
