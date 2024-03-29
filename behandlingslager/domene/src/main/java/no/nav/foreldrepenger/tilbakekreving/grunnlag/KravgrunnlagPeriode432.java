package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "kravgrunnlagPeriode432")
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KravgrunnlagPeriode432 that = (KravgrunnlagPeriode432) o;
        return Objects.equals(periode, that.periode) &&
            erBigDecimalLik(beløpSkattMnd, that.beløpSkattMnd) &&
            erListeLik(kravgrunnlagBeloper433, that.kravgrunnlagBeloper433);
    }

    private boolean erBigDecimalLik(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null && bd2 == null)
            return true;
        if (bd1 == null || bd2 == null)
            return false;
        return bd1.compareTo(bd2) == 0;
    }

    private boolean erListeLik(List<KravgrunnlagBelop433> l1, List<KravgrunnlagBelop433> l2) {
        if (l1 == null && l2 == null)
            return true;
        if (l1 == null || l2 == null)
            return false;
        return l1.size() == l2.size() && l2.containsAll(l1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, beløpSkattMnd, kravgrunnlagBeloper433);
    }


    @Override
    public String toString() {
        return "KravgrunnlagPeriode432{" +
            "periode=" + periode +
            ", beløpSkattMnd=" + beløpSkattMnd +
            ", kravgrunnlagBeloper433=" + kravgrunnlagBeloper433 +
            '}';
    }
}
