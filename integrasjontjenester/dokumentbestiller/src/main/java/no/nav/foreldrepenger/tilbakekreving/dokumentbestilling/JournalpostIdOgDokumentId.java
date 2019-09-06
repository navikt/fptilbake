package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;

class JournalpostIdOgDokumentId {
    private JournalpostId journalpostId;
    private String dokumentId;

    JournalpostIdOgDokumentId(JournalpostId journalpostId, String dokumentId) {
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
