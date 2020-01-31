package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@Entity(name = "KravgrunnlagPeriode432")
@Table(name = "KRAV_GRUNNLAG_PERIODE_432")
public class KravgrunnlagPeriode432 extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KRAV_GRUNNLAG_PERIODE_432")
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fom", column = @Column(name = "fom", nullable = false, updatable = false)),
        @AttributeOverride(name = "tom", column = @Column(name = "tom", nullable = false, updatable = false))
    })
    private Periode periode;

    @Column(name = "belop_skatt_mnd")
    private BigDecimal beløpSkattMnd = BigDecimal.ZERO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "krav_grunnlag_431_id", nullable = false, updatable = false)
    private Kravgrunnlag431 kravgrunnlag431;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "kravgrunnlagPeriode432", cascade = CascadeType.PERSIST)
    private List<KravgrunnlagBelop433> kravgrunnlagBeloper433 = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Periode getPeriode() {
        return periode;
    }

    public LocalDate getFom() {
        return periode.getFom();
    }

    public BigDecimal getBeløpSkattMnd() {
        return beløpSkattMnd;
    }

    public Kravgrunnlag431 getKravgrunnlag431() {
        return kravgrunnlag431;
    }

    public List<KravgrunnlagBelop433> getKravgrunnlagBeloper433() {
        return Collections.unmodifiableList(kravgrunnlagBeloper433);
    }

    public void setKravgrunnlagBeloper433(List<KravgrunnlagBelop433> kravgrunnlagBeloper433) {
        this.kravgrunnlagBeloper433 = kravgrunnlagBeloper433;
    }

    public void leggTilBeløp(KravgrunnlagBelop433 KravgrunnlagBelop433) { //NOSONAR
        Objects.requireNonNull(KravgrunnlagBelop433, "KravgrunnlagBelop433");
        kravgrunnlagBeloper433.add(KravgrunnlagBelop433);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private KravgrunnlagPeriode432 kladd = new KravgrunnlagPeriode432();

        public Builder medPeriode(Periode periode) {
            this.kladd.periode = periode;
            return this;
        }

        public Builder medPeriode(LocalDate fom, LocalDate tom) {
            this.kladd.periode = Periode.of(fom, tom);
            return this;
        }

        public Builder medBeløpSkattMnd(BigDecimal beløpSkattMnd) {
            this.kladd.beløpSkattMnd = beløpSkattMnd;
            return this;
        }

        public Builder medKravgrunnlag431(Kravgrunnlag431 kravgrunnlag431) {
            this.kladd.kravgrunnlag431 = kravgrunnlag431;
            return this;
        }

        public KravgrunnlagPeriode432 build() {
            Objects.requireNonNull(this.kladd.periode, "periode");
            Objects.requireNonNull(this.kladd.kravgrunnlagBeloper433, "kravgrunnlagBeloper433");
            return kladd;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
            + "periode=" + periode + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "beløpSkattMnd=" + beløpSkattMnd + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + ">";//$NON-NLS-1$
    }
}
