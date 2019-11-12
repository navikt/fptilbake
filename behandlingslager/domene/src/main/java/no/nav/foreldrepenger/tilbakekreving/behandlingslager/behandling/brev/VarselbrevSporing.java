package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.vedtak.felles.jpa.BaseEntitet;


@Entity(name = "VarselbrevSporing")
@Table(name = "VARSELBREV_SPORING")
public class VarselbrevSporing extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VARSELBREV_DATA")
    private Long id;

    @Column(name = "BEHANDLING_ID", nullable = false, updatable = false)
    private Long behandlingId;

    @Column(name = "JOURNALPOST_ID", nullable = false)
    private String journalpostId;

    @Column(name = "DOKUMENT_ID", nullable = false)
    private String dokumentId;

    private VarselbrevSporing() {
        // Hibernate
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public void setDokumentId(String dokumentId) {
        this.dokumentId = dokumentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public static class Builder {

        private VarselbrevSporing varselbrevSporing = new VarselbrevSporing();

        public VarselbrevSporing.Builder medBehandlingId(Long behandlingId) {
            varselbrevSporing.behandlingId = behandlingId;
            return this;
        }

        public VarselbrevSporing.Builder medJournalpostId(JournalpostId journalpostId) {
            varselbrevSporing.journalpostId = journalpostId.getVerdi();
            return this;
        }

        public VarselbrevSporing.Builder medDokumentId(String dokumentId) {
            varselbrevSporing.dokumentId = dokumentId;
            return this;
        }

        public VarselbrevSporing build() {
            Objects.requireNonNull(varselbrevSporing.behandlingId);
            Objects.requireNonNull(varselbrevSporing.journalpostId);
            return varselbrevSporing;
        }
    }
}
