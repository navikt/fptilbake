package no.nav.foreldrepenger.tilbakekreving.historikk;

public class HistorikkInnslagDokumentLinkDto {

    private String tag;
    private String journalpostId;
    private String dokumentId;
    private boolean utgått;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public void setDokumentId(String dokumentId) {
        this.dokumentId = dokumentId;
    }

    public boolean isUtgått() {
        return utgått;
    }

    public void setUtgått(boolean utgått) {
        this.utgått = utgått;
    }
}
