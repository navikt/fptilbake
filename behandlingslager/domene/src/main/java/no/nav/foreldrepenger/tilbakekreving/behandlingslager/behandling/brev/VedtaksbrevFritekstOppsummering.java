package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

@Entity(name = "VedtaksbrevFritekstOppsummering")
@Table(name = "VEDTAKSBREV_OPPSUMMERING")
public class VedtaksbrevFritekstOppsummering extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAKSBREV_OPPSUMMERING")
    private Long id;

    @Column(name = "BEHANDLING_ID", nullable = false, updatable = false)
    private Long behandlingId;

    @Column(name = "OPPSUMMERING_FRITEKST")
    private String oppsummeringFritekst;

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

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public void setOppsummeringFritekst(String oppsummeringFritekst) {
        this.oppsummeringFritekst = oppsummeringFritekst;
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
