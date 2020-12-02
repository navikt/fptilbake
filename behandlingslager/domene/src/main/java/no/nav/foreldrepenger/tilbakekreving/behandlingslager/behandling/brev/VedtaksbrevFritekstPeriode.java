package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@Entity(name = "VedtaksbrevFritekstPeriode")
@Table(name = "VEDTAKSBREV_PERIODE")
public class VedtaksbrevFritekstPeriode extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAKSBREV_PERIODE")
    private Long id;

    @Column(name = "BEHANDLING_ID", nullable = false, updatable = false)
    private Long behandlingId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fom", column = @Column(name = "fom", nullable = false, updatable = false)),
        @AttributeOverride(name = "tom", column = @Column(name = "tom", nullable = false, updatable = false))
    })
    private Periode periode;

    @Convert(converter = VedtaksbrevFritekstType.KodeverdiConverter.class)
    @Column(name = "FRITEKST_TYPE", nullable = false)
    private VedtaksbrevFritekstType fritekstType;

    @Column(name = "FRITEKST", nullable = false)
    private String fritekst;

    public VedtaksbrevFritekstPeriode() {
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


    public Periode getPeriode() {
        return periode;
    }

    public void setPeriode(Periode periode) {
        this.periode = periode;
    }

    public VedtaksbrevFritekstType getFritekstType() {
        return fritekstType;
    }

    public void setFritekstType(VedtaksbrevFritekstType fritekstType) {
        this.fritekstType = fritekstType;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setFritekst(String fritekst) {
        this.fritekst = fritekst;
    }

    public static class Builder {

        private VedtaksbrevFritekstPeriode vedtaksbrevFritekstPeriode = new VedtaksbrevFritekstPeriode();

        public VedtaksbrevFritekstPeriode.Builder medBehandlingId(Long behandlingId) {
            vedtaksbrevFritekstPeriode.behandlingId = behandlingId;
            return this;
        }

        public VedtaksbrevFritekstPeriode.Builder medPeriode(Periode periode) {
            vedtaksbrevFritekstPeriode.periode = periode;
            return this;
        }

        public VedtaksbrevFritekstPeriode.Builder medFritekstType(VedtaksbrevFritekstType fritekstType) {
            vedtaksbrevFritekstPeriode.fritekstType = fritekstType;
            return this;
        }

        public VedtaksbrevFritekstPeriode.Builder medFritekst(String fritekst) {
            vedtaksbrevFritekstPeriode.fritekst = fritekst;
            return this;
        }

        public VedtaksbrevFritekstPeriode build() {
            Objects.requireNonNull(vedtaksbrevFritekstPeriode.behandlingId);
            Objects.requireNonNull(vedtaksbrevFritekstPeriode.periode);
            Objects.requireNonNull(vedtaksbrevFritekstPeriode.fritekstType);
            if (vedtaksbrevFritekstPeriode.fritekst == null || vedtaksbrevFritekstPeriode.fritekst.isBlank()) {
                throw new IllegalArgumentException("Mangler fritekst");
            }
            return vedtaksbrevFritekstPeriode;
        }
    }
}
