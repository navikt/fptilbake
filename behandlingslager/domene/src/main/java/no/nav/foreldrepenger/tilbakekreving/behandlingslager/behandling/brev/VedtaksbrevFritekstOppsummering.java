package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
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

    @Lob
    @Column(name = "OPPSUMMERING_FRITEKST")
    private String oppsummeringFritekst;

    @Convert(converter = VedtaksbrevType.verdiConverter.class)
    @Column(name = "BREV_TYPE")
    private VedtaksbrevType brevType = VedtaksbrevType.ORDINÆR;

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

    public VedtaksbrevType getBrevType() {
        return brevType;
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

        public VedtaksbrevFritekstOppsummering.Builder medBrevType(VedtaksbrevType brevType) {
            vedtaksbrevFritekstOppsummering.brevType = brevType;
            return this;
        }

        public VedtaksbrevFritekstOppsummering build() {
            Objects.requireNonNull(vedtaksbrevFritekstOppsummering.behandlingId);
            Objects.requireNonNull(vedtaksbrevFritekstOppsummering.brevType);
            return vedtaksbrevFritekstOppsummering;
        }
    }
}
