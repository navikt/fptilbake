package no.nav.journalpostapi.dto.opprett;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpprettJournalpostResponse {
    private String journalpostId;
    private String journalstatus;
    private String melding;
    private boolean journalpostFerdigstilt;
    private List<DokumentInfoId> dokumenter;

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getJournalstatus() {
        return journalstatus;
    }

    public String getMelding() {
        return melding;
    }

    public boolean isJournalpostFerdigstilt() {
        return journalpostFerdigstilt;
    }

    public List<DokumentInfoId> getDokumenter() {
        return dokumenter;
    }

    public static class DokumentInfoId {
        private String dokumentInfoId;

        public String getDokumentInfoId() {
            return dokumentInfoId;
        }
    }
}
