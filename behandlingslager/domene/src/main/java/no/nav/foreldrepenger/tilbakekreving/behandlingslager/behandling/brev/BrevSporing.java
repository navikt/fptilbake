package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkBaseEntitet;

@Entity(name = "BrevSporing")
@Table(name = "BREV_SPORING")
public class BrevSporing extends KodeverkBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BREV_SPORING")
    private Long id;

    @Column(name = "BEHANDLING_ID", nullable = false, updatable = false)
    private Long behandlingId;

    @Column(name = "JOURNALPOST_ID", nullable = false)
    private String journalpostId;

    @Column(name = "DOKUMENT_ID", nullable = false)
    private String dokumentId;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "brev_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BrevType.DISCRIMINATOR + "'"))
    private BrevType brevType = BrevType.UDEFINERT;

    public BrevSporing() {
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

    public BrevType getBrevType() {
        return brevType;
    }

    public static class Builder {

        private BrevSporing brevSporing = new BrevSporing();

        public BrevSporing.Builder medBehandlingId(Long behandlingId) {
            brevSporing.behandlingId = behandlingId;
            return this;
        }

        @Deprecated //bruk alternativ metode med sterk typing
        public BrevSporing.Builder medJournalpostId(String journalpostId) {
            brevSporing.journalpostId = journalpostId;
            return this;
        }

        public BrevSporing.Builder medJournalpostId(JournalpostId journalpostId) {
            brevSporing.journalpostId = journalpostId.getVerdi();
            return this;
        }


        public BrevSporing.Builder medDokumentId(String dokumentId) {
            brevSporing.dokumentId = dokumentId;
            return this;
        }

        public BrevSporing.Builder medBrevType(BrevType brevType) {
            brevSporing.brevType = brevType;
            return this;
        }

        public BrevSporing build() {
            Objects.requireNonNull(brevSporing.behandlingId);
            Objects.requireNonNull(brevSporing.journalpostId);
            Objects.requireNonNull(brevSporing.dokumentId);
            Objects.requireNonNull(brevSporing.brevType);
            return brevSporing;
        }
    }
}
