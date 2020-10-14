package no.nav.journalpostapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tilleggsopplysning {
    @JsonProperty("nokkel")
    private String nøkkel;
    private String verdi;

    public Tilleggsopplysning(String nøkkel, String verdi) {
        this.nøkkel = nøkkel;
        this.verdi = verdi;
    }

    public String getNøkkel() {
        return nøkkel;
    }

    public String getVerdi() {
        return verdi;
    }
}
