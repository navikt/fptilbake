package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

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
        switch (brevType) {
            case FRITEKST_FEILUTBETALING_BORTFALT:
                return 10000;
            case ORDINÆR:
                return 4000;
            default:
                throw new IllegalArgumentException("Utviklerfeil: ustøttet VedtaksbrevType(" + brevType + ") i VedtaksbrevFritekstOppsummering");
        }
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
