package no.nav.journalpostapi;

import com.fasterxml.jackson.annotation.JsonValue;

public interface Kode {

    @JsonValue
    String getKode();
}
