package no.nav.journalpostapi.dto.sak;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sak {

    private Sakstype sakstype;
    private String fagsakId;
    private FagsakSystem fagsaksystem;

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

}
