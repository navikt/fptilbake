package no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.BatchSize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseCreateableEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

@Entity(name = "Beregningsresultat")
@Table(name = "BEREGNINGSRESULTAT")
public class BeregningsresultatEntitet extends BaseCreateableEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEREGNINGSRESULTAT")
    private Long id;

    @Convert(converter = VedtakResultatType.KodeverdiConverter.class)
    @Column(name = "vedtak_resultat_type", nullable = false)
    private VedtakResultatType vedtakResultatType;

    @OneToMany
    @JoinColumn(name = "beregningsresultat_id", nullable = false)
    @BatchSize(size = 20)
    private List<BeregningsresultatPeriodeEntitet> perioder = new ArrayList<>();

    protected BeregningsresultatEntitet() {
    }

    public BeregningsresultatEntitet(VedtakResultatType vedtakResultatType, List<BeregningsresultatPeriodeEntitet> perioder) {
        Objects.requireNonNull(vedtakResultatType, "vedtakResultatType");
        Objects.requireNonNull(perioder, "perioder");
        this.vedtakResultatType = vedtakResultatType;
        this.perioder = perioder;
    }

    public List<BeregningsresultatPeriodeEntitet> getPerioder() {
        return perioder;
    }

    private List<BeregningsresultatPeriodeEntitet> getSortertePerioder() {
        return perioder.stream()
            .sorted(Comparator.comparing(p -> p.getPeriode().getFom()))
            .toList();
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
        BeregningsresultatEntitet that = (BeregningsresultatEntitet) o;
        return vedtakResultatType == that.vedtakResultatType
            && Objects.equals(getSortertePerioder(), that.getSortertePerioder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(vedtakResultatType, getSortertePerioder());
    }

    @Override
    public String toString() {
        return "Beregningsresultat{" +
            "vedtakResultatType=" + vedtakResultatType +
            ", perioder=" + perioder +
            '}';
    }
}
