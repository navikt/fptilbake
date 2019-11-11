package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;

public class JournalpostIdOgDokumentId {
    private JournalpostId journalpostId;
    private String dokumentId;

    public JournalpostIdOgDokumentId(JournalpostId journalpostId, String dokumentId) {
        this.journalpostId = journalpostId;
        this.dokumentId = dokumentId;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }
}
