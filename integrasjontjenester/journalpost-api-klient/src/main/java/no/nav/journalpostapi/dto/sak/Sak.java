package no.nav.journalpostapi.dto.sak;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sak {

    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Sakstype sakstype;
    private String fagsakId;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private FagsakSystem fagsaksystem;
    private String arkivsaksnummer;
    private String arkivsaksystem;

    private Sak() {
    }

    public Sak(String fagsakId, FagsakSystem fagsaksystem) {
        this.fagsakId = fagsakId;
        this.fagsaksystem = fagsaksystem;
        this.sakstype = Sakstype.FAGSAK;
    }

    public Sakstype getSakstype() {
        return sakstype;
    }

    public String getFagsakId() {
        return fagsakId;
    }

    public FagsakSystem getFagsaksystem() {
        return fagsaksystem;
    }

    public String getArkivsaksnummer() {
        return arkivsaksnummer;
    }

    public String getArkivsaksystem() {
        return arkivsaksystem;
    }

}
