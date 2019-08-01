package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.felles.jpa.BaseEntitet;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity(name = "VedtaksbrevPeriode")
@Table(name = "VEDTAKSBREV_PERIODE")
public class VedtaksbrevPeriode extends BaseEntitet {

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

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "FRITEKST_TYPE", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + FritekstType.DISCRIMINATOR + "'"))
    private FritekstType fritekstType;

    @Column(name = "FRITEKST", nullable = false)
    private String fritekst;

    public VedtaksbrevPeriode() {
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

    public FritekstType getFritekstType() {
        return fritekstType;
    }

    public void setFritekstType(FritekstType fritekstType) {
        this.fritekstType = fritekstType;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setFritekst(String fritekst) {
        this.fritekst = fritekst;
    }

    public static class Builder {

        private VedtaksbrevPeriode vedtaksbrevPeriode = new VedtaksbrevPeriode();

        public VedtaksbrevPeriode.Builder medBehandlingId(Long behandlingId) {
            vedtaksbrevPeriode.behandlingId = behandlingId;
            return this;
        }

        public VedtaksbrevPeriode.Builder medPeriode(Periode periode) {
            vedtaksbrevPeriode.periode = periode;
            return this;
        }

        public VedtaksbrevPeriode.Builder medFritekstType(FritekstType fritekstType) {
            vedtaksbrevPeriode.fritekstType = fritekstType;
            return this;
        }

        public VedtaksbrevPeriode.Builder medFritekst(String fritekst) {
            vedtaksbrevPeriode.fritekst = fritekst;
            return this;
        }

        public VedtaksbrevPeriode build() {
            Objects.requireNonNull(vedtaksbrevPeriode.behandlingId);
            Objects.requireNonNull(vedtaksbrevPeriode.periode);
            return vedtaksbrevPeriode;
        }
    }
}
