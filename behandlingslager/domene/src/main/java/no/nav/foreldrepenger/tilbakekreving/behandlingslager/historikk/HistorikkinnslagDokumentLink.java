package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.diff.IndexKey;

@Entity(name = "HistorikkinnslagDokumentLink")
@Table(name = "HISTORIKKINNSLAG_DOK_LINK")
public class HistorikkinnslagDokumentLink extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_HISTORIKKINNSLAG_DOK_LINK")
    private Long id;

    @Column(name = "link_tekst", updatable = false, nullable = false)
    private String linkTekst;

    @ManyToOne(optional = false)
    @JoinColumn(name = "historikkinnslag_id", nullable = false, updatable = false)
    private Historikkinnslag historikkinnslag;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "journalpostId", column = @Column(name = "journalpost_id", updatable = false)))
    private JournalpostId journalpostId;

    @Column(name = "dokument_id", updatable = false)
    private String dokumentId;

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(journalpostId, dokumentId, linkTekst);
    } //endre import indexkey? flere alternativer

    public String getLinkTekst() {
        return linkTekst;
    }

    public void setLinkTekst(String tag) {
        this.linkTekst = tag;
    }

    public void setHistorikkinnslag(Historikkinnslag historikkinnslag) {
        this.historikkinnslag = historikkinnslag;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public void setDokumentId(String dokumentId) {
        this.dokumentId = dokumentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistorikkinnslagDokumentLink)) {
            return false;
        }
        HistorikkinnslagDokumentLink that = (HistorikkinnslagDokumentLink) o;
        return
                Objects.equals(getLinkTekst(), that.getLinkTekst()) &&
                        Objects.equals(historikkinnslag, that.historikkinnslag) &&
                        Objects.equals(getJournalpostId(), that.getJournalpostId()) &&
                        Objects.equals(getDokumentId(), that.getDokumentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLinkTekst(), historikkinnslag, getJournalpostId(), getDokumentId());
    }
}
