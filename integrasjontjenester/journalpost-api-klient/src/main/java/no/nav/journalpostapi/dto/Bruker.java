package no.nav.journalpostapi.dto;

public class Bruker {
    private String id;
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
