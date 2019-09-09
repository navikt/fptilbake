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

@Entity(name = "VedtaksbrevSporing")
@Table(name = "VEDTAKSBREV_SPORING")
public class VedtaksbrevSporing extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAKSBREV_SPORING")
    private Long id;

    @Column(name = "BEHANDLING_ID", nullable = false, updatable = false)
    private Long behandlingId;

    @Column(name = "JOURNALPOST_ID", nullable = false)
    private String journalpostId;

    @Column(name = "DOKUMENT_ID", nullable = false)
    private String dokumentId;

    public VedtaksbrevSporing() {
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

        private VedtaksbrevSporing vedtaksbrevSporing = new VedtaksbrevSporing();

        public VedtaksbrevSporing.Builder medBehandlingId(Long behandlingId) {
            vedtaksbrevSporing.behandlingId = behandlingId;
            return this;
        }

        @Deprecated //bruk alternativ metode med sterk typing
        public VedtaksbrevSporing.Builder medJournalpostId(String journalpostId) {
            vedtaksbrevSporing.journalpostId = journalpostId;
            return this;
        }

        public VedtaksbrevSporing.Builder medJournalpostId(JournalpostId journalpostId) {
            vedtaksbrevSporing.journalpostId = journalpostId.getVerdi();
            return this;
        }


        public VedtaksbrevSporing.Builder medDokumentId(String dokumentId) {
            vedtaksbrevSporing.dokumentId = dokumentId;
            return this;
        }

        public VedtaksbrevSporing build() {
            Objects.requireNonNull(vedtaksbrevSporing.behandlingId);
            Objects.requireNonNull(vedtaksbrevSporing.journalpostId);
            Objects.requireNonNull(vedtaksbrevSporing.dokumentId);
            return vedtaksbrevSporing;
        }
    }
}
