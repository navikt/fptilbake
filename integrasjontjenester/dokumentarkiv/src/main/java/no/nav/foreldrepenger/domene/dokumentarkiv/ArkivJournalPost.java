package no.nav.foreldrepenger.domene.dokumentarkiv;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class ArkivJournalPost {
    private JournalpostId journalpostId;
    private Saksnummer saksnummer;

    public ArkivJournalPost(Saksnummer saksnummer, JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
        this.saksnummer = saksnummer;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArkivJournalPost that = (ArkivJournalPost) o;
        return Objects.equals(journalpostId, that.journalpostId) &&
            Objects.equals(saksnummer, that.saksnummer) ;
    }

    @Override
    public int hashCode() {

        return Objects.hash(journalpostId, saksnummer);
    }

}
