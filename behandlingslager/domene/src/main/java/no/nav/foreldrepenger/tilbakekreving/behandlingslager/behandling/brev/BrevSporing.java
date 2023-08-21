package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;

@Entity(name = "BrevSporing")
@Table(name = "BREV_SPORING")
public class BrevSporing extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BREV_SPORING")
    private Long id;

    @Column(name = "BEHANDLING_ID", nullable = false, updatable = false)
    private Long behandlingId;

    @Column(name = "JOURNALPOST_ID", nullable = false)
    private String journalpostId;

    @Column(name = "DOKUMENT_ID", nullable = false)
    private String dokumentId;

    @Convert(converter = BrevType.KodeverdiConverter.class)
    @Column(name = "brev_type", nullable = false)
    private BrevType brevType = BrevType.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    private BrevSporing() {
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

        private final BrevSporing brevSporing = new BrevSporing();

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
