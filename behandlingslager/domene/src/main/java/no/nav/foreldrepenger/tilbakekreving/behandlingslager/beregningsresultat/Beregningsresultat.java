package no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

@Entity(name = "Beregningsresultat")
@Table(name = "BEREGNINGSRESULTAT")
public class Beregningsresultat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEREGNINGSRESULTAT")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Convert(converter = VedtakResultatType.KodeverdiConverter.class)
    @Column(name = "vedtak_resultat_type", nullable = false)
    private VedtakResultatType vedtakResultatType;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = "beregningsresultat_id", nullable = false)
    @OrderBy(value = "periode.fom asc") //equals+hashCode avhengig av sortering
    @BatchSize(size = 20)
    private List<BeregningsresultatPeriode> perioder = new ArrayList<>();

    Beregningsresultat() {
    }

    public Beregningsresultat(VedtakResultatType vedtakResultatType, List<BeregningsresultatPeriode> perioder) {
        this.vedtakResultatType = vedtakResultatType;
        this.perioder = perioder.stream().sorted(Comparator.comparing(p -> p.getPeriode().getFom())).toList(); //equals+hashCode avhengig av sortering
    }

    public List<BeregningsresultatPeriode> getPerioder() {
        return perioder;
    }

    public VedtakResultatType getVedtakResultatType() {
        return vedtakResultatType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Beregningsresultat that = (Beregningsresultat) o;
        return vedtakResultatType == that.vedtakResultatType
            && Objects.equals(perioder, that.perioder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vedtakResultatType, perioder);
    }

    @Override
    public String toString() {
        return "Beregningsresultat{" +
            "vedtakResultatType=" + vedtakResultatType +
            ", perioder=" + perioder +
            '}';
    }
}
