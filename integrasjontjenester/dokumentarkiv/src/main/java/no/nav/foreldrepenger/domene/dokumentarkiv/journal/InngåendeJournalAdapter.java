package no.nav.foreldrepenger.domene.dokumentarkiv.journal;

import java.util.List;

import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;

public interface InngåendeJournalAdapter {

    List<JournalMetadata<DokumentTypeId>> hentMetadata(JournalpostId journalpostId);

    ArkivJournalPost hentInngåendeJournalpostHoveddokument(JournalpostId journalpostId);
}
