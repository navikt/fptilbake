package no.nav.journalpostapi.dto.opprett;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpprettJournalpostResponse {
    private String journalpostId;
    private String melding;
    private boolean journalpostFerdigstilt;
    private List<String> dokumenter;

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getMelding() {
        return melding;
    }

    public boolean isJournalpostFerdigstilt() {
        return journalpostFerdigstilt;
    }

    public List<String> getDokumenter() {
        return dokumenter;
    }
}
