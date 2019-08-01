package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import no.nav.vedtak.felles.jpa.BaseEntitet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity(name = "VedtaksbrevOppsummering")
@Table(name = "VEDTAKSBREV_OPPSUMMERING")
public class VedtaksbrevOppsummering extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAKSBREV_OPPSUMMERING")
    private Long id;

    @Column(name = "BEHANDLING_ID", nullable = false, updatable = false)
    private Long behandlingId;

    @Column(name = "OPPSUMMERING_FRITEKST")
    private String oppsummeringFritekst;

    public VedtaksbrevOppsummering() {
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

        private VedtaksbrevOppsummering vedtaksbrevOppsummering = new VedtaksbrevOppsummering();

        public VedtaksbrevOppsummering.Builder medBehandlingId(Long behandlingId) {
            vedtaksbrevOppsummering.behandlingId = behandlingId;
            return this;
        }

        public VedtaksbrevOppsummering.Builder medOppsummeringFritekst(String oppsummeringFritekst) {
            vedtaksbrevOppsummering.oppsummeringFritekst = oppsummeringFritekst;
            return this;
        }

        public VedtaksbrevOppsummering build() {
            Objects.requireNonNull(vedtaksbrevOppsummering.behandlingId);
            return vedtaksbrevOppsummering;
        }
    }
}
