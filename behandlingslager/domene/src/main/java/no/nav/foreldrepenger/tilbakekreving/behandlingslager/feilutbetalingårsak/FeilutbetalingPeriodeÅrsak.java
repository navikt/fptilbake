package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

import java.time.LocalDate;

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

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "FeilutbetalingPeriodeÅrsak")
@Table(name = "FEILUTBETALING_PERIODE_AARSAK")
public class FeilutbetalingPeriodeÅrsak extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FEILUTBET_PERIODE_AARSAK")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "feilutbetaling_id", nullable = false, updatable = false)
    private Feilutbetaling feilutbetalinger;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fom", column = @Column(name = "fom", nullable = false, updatable = false)),
        @AttributeOverride(name = "tom", column = @Column(name = "tom", nullable = false, updatable = false))
    })
    private Periode periode;

    @Column(name = "aarsak", nullable = false, updatable = false)
    private String årsak;

    @Column(name = "aarsak_kodeverk", nullable = false, updatable = false)
    private String årsakKodeverk;

    @Column(name = "under_aarsak", updatable = false)
    private String underÅrsak;

    @Column(name = "under_aarsak_kodeverk", updatable = false)
    private String underÅrsakKodeverk;

    FeilutbetalingPeriodeÅrsak() {
        // FOR CDI
    }

    public Long getId() {
        return id;
    }

    public Feilutbetaling getFeilutbetalinger() {
        return feilutbetalinger;
    }

    public Periode getPeriode() {
        return periode;
    }

    public String getÅrsak() {
        return årsak;
    }

    public String getÅrsakKodeverk() {
        return årsakKodeverk;
    }

    public String getUnderÅrsak() {
        return underÅrsak;
    }

    public String getUnderÅrsakKodeverk() {
        return underÅrsakKodeverk;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private FeilutbetalingPeriodeÅrsak kladd = new FeilutbetalingPeriodeÅrsak();

        public Builder medFeilutbetalinger(Feilutbetaling feilutbetaling) {
            this.kladd.feilutbetalinger = feilutbetaling;
            return this;
        }

        public Builder medPeriode(LocalDate fom, LocalDate tom) {
            this.kladd.periode = Periode.of(fom, tom);
            return this;
        }

        public Builder medPeriode(Periode periode) {
            this.kladd.periode = periode;
            return this;
        }

        public Builder medÅrsak(String årsak) {
            this.kladd.årsak = årsak;
            return this;
        }

        public Builder medÅrsakKodeverk(String årsakKodeverk) {
            this.kladd.årsakKodeverk = årsakKodeverk;
            return this;
        }

        public Builder medUnderÅrsak(String underÅrsak) {
            this.kladd.underÅrsak = underÅrsak;
            return this;
        }

        public Builder medUnderÅrsakKodeverk(String underÅrsakKodeverk) {
            this.kladd.underÅrsakKodeverk = underÅrsakKodeverk;
            return this;
        }

        public FeilutbetalingPeriodeÅrsak build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id //$NON-NLS-1$
            + ", periode=" + periode //$NON-NLS-1$
            + ", årsak=" + årsak //$NON-NLS-1$
            + ", årsakKodeverk=" + årsakKodeverk //$NON-NLS-1$
            + ", underÅrsak=" + underÅrsak //$NON-NLS-1$
            + ", underÅrsakKodeverk=" + underÅrsakKodeverk //$NON-NLS-1$
            + ">"; //$NON-NLS-1$

    }
}
