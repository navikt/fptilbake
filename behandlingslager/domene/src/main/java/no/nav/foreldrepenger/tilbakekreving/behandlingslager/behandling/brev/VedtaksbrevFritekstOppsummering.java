package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

@Entity(name = "VedtaksbrevFritekstOppsummering")
@Table(name = "VEDTAKSBREV_OPPSUMMERING")
public class VedtaksbrevFritekstOppsummering extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAKSBREV_OPPSUMMERING")
    private Long id;

    @Column(name = "BEHANDLING_ID", nullable = false, updatable = false)
    private Long behandlingId;

    @Lob
    @Column(name = "FRITEKST")
    private String oppsummeringFritekst;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public VedtaksbrevFritekstOppsummering() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public String getOppsummeringFritekst() {
        return oppsummeringFritekst;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static int maxFritekstLengde(VedtaksbrevType brevType) {
        return switch (brevType) {
            case FRITEKST_FEILUTBETALING_BORTFALT -> 10000;
            case ORDINÆR -> 4000;
            default -> throw new IllegalArgumentException("Utviklerfeil: ustøttet VedtaksbrevType(" + brevType + ") i VedtaksbrevFritekstOppsummering");
        };
    }

    public static class Builder {

        private VedtaksbrevFritekstOppsummering vedtaksbrevFritekstOppsummering = new VedtaksbrevFritekstOppsummering();

        public VedtaksbrevFritekstOppsummering.Builder medBehandlingId(Long behandlingId) {
            vedtaksbrevFritekstOppsummering.behandlingId = behandlingId;
            return this;
        }

        public VedtaksbrevFritekstOppsummering.Builder medOppsummeringFritekst(String oppsummeringFritekst) {
            vedtaksbrevFritekstOppsummering.oppsummeringFritekst = oppsummeringFritekst;
            return this;
        }

        public VedtaksbrevFritekstOppsummering build() {
            Objects.requireNonNull(vedtaksbrevFritekstOppsummering.behandlingId);
            return vedtaksbrevFritekstOppsummering;
        }
    }
}
