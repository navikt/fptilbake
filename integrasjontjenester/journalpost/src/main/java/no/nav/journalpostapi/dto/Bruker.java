package no.nav.journalpostapi.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

public class Bruker {
    private String id;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private SenderMottakerIdType idType;

}
