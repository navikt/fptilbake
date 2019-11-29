package no.nav.journalpostapi.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

public class Bruker {
    private String id;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private BrukerIdType idType;

    public Bruker(BrukerIdType idType, String id) {
        this.id = id;
        this.idType = idType;
    }

    public String getId() {
        return id;
    }

    public BrukerIdType getIdType() {
        return idType;
    }
}
