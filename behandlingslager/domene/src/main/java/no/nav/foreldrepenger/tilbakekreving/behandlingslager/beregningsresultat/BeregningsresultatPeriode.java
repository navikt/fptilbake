package no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat;

import java.math.BigDecimal;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "BeregningsresultatPeriode")
@Table(name = "BEREGNINGSRESULTAT_PERIODE")
public class BeregningsresultatPeriode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEREGNINGSRESULTAT_PERIODE")
    private Long id;

    private Periode periode;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "er_foreldet", nullable = false, updatable = false)
    private boolean erForeldet;

    @Column(name = "tilbakekreving_beloep")
    private BigDecimal tilbakekrevingBeløp;

    @Column(name = "tilbakekreving_beloep_u_skatt")
    private BigDecimal tilbakekrevingBeløpEtterSkatt;
    @Column(name = "tilbakekreving_beloep_u_rente")
    private BigDecimal tilbakekrevingBeløpUtenRenter;
    @Column(name = "renter_prosent")
    private BigDecimal renterProsent;
    @Column(name = "renter_beloep")
    private BigDecimal renteBeløp;
    @Column(name = "skatt_beloep")
    private BigDecimal skattBeløp;
    @Column(name = "feilutbetalt_beloep")
    private BigDecimal feilutbetaltBeløp;
    @Column(name = "utbetalt_ytelse_beloep")
    private BigDecimal utbetaltYtelseBeløp; //rått beløp, ikke justert for evt. trekk
    @Column(name = "riktig_ytelse_beloep")
    private BigDecimal riktigYtelseBeløp; //rått beløp, ikke justert for evt. trekk

    @ManyToOne
    @JoinColumn(name = "beregningsresultat_id", insertable = false, updatable = false)
    private Beregningsresultat beregningsresultat;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    BeregningsresultatPeriode() {
    }

    public Periode getPeriode() {
        return periode;
    }

    public boolean erForeldet() {
        return erForeldet;
    }

    public BigDecimal getTilbakekrevingBeløp() {
        return tilbakekrevingBeløp;
    }

    public BigDecimal getTilbakekrevingBeløpEtterSkatt() {
        return tilbakekrevingBeløpEtterSkatt;
    }

    public BigDecimal getTilbakekrevingBeløpUtenRenter() {
        return tilbakekrevingBeløpUtenRenter;
    }

    public BigDecimal getRenterProsent() {
        return renterProsent;
    }

    public BigDecimal getRenteBeløp() {
        return renteBeløp;
    }

    public BigDecimal getSkattBeløp() {
        return skattBeløp;
    }

    public BigDecimal getFeilutbetaltBeløp() {
        return feilutbetaltBeløp;
    }

    public BigDecimal getUtbetaltYtelseBeløp() {
        return utbetaltYtelseBeløp;
    }

    public BigDecimal getRiktigYtelseBeløp() {
        return riktigYtelseBeløp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeregningsresultatPeriode that = (BeregningsresultatPeriode) o;
        return erForeldet == that.erForeldet
            && Objects.equals(periode, that.periode)
            && Objects.equals(tilbakekrevingBeløp, that.tilbakekrevingBeløp)
            && Objects.equals(tilbakekrevingBeløpEtterSkatt, that.tilbakekrevingBeløpEtterSkatt)
            && Objects.equals(tilbakekrevingBeløpUtenRenter, that.tilbakekrevingBeløpUtenRenter)
            && Objects.equals(renterProsent, that.renterProsent)
            && Objects.equals(renteBeløp, that.renteBeløp)
            && Objects.equals(skattBeløp, that.skattBeløp)
            && Objects.equals(feilutbetaltBeløp, that.feilutbetaltBeløp)
            && Objects.equals(utbetaltYtelseBeløp, that.utbetaltYtelseBeløp)
            && Objects.equals(riktigYtelseBeløp, that.riktigYtelseBeløp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, erForeldet, tilbakekrevingBeløp, tilbakekrevingBeløpEtterSkatt, tilbakekrevingBeløpUtenRenter, renterProsent, renteBeløp, skattBeløp, feilutbetaltBeløp, utbetaltYtelseBeløp, riktigYtelseBeløp);
    }

    @Override
    public String toString() {
        return "BeregningsresultatPeriode{" +
            "periode=" + periode +
            '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsresultatPeriode kladd = new BeregningsresultatPeriode();
        private Boolean erForeldet;

        private Builder() {
        }

        public Builder medPeriode(Periode periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder medErForeldet(boolean erForeldet) {
            kladd.erForeldet = erForeldet;
            this.erForeldet = erForeldet;
            return this;
        }

        public Builder medTilbakekrevingBeløp(BigDecimal tilbakekrevingBeløp) {
            kladd.tilbakekrevingBeløp = tilbakekrevingBeløp;
            return this;
        }

        public Builder medTilbakekrevingBeløpEtterSkatt(BigDecimal tilbakekrevingBeløpEtterSkatt) {
            kladd.tilbakekrevingBeløpEtterSkatt = tilbakekrevingBeløpEtterSkatt;
            return this;
        }

        public Builder medTilbakekrevingBeløpUtenRenter(BigDecimal tilbakekrevingBeløpUtenRenter) {
            kladd.tilbakekrevingBeløpUtenRenter = tilbakekrevingBeløpUtenRenter;
            return this;
        }

        public Builder medRenterProsent(BigDecimal renterProsent) {
            kladd.renterProsent = renterProsent;
            return this;
        }

        public Builder medRenteBeløp(BigDecimal renteBeløp) {
            kladd.renteBeløp = renteBeløp;
            return this;
        }

        public Builder medSkattBeløp(BigDecimal skattBeløp) {
            kladd.skattBeløp = skattBeløp;
            return this;
        }

        public Builder medFeilutbetaltBeløp(BigDecimal feilutbetaltBeløp) {
            kladd.feilutbetaltBeløp = feilutbetaltBeløp;
            return this;
        }

        public Builder medUtbetaltYtelseBeløp(BigDecimal utbetaltYtelseBeløp) {
            kladd.utbetaltYtelseBeløp = utbetaltYtelseBeløp;
            return this;
        }

        public Builder medRiktigYtelseBeløp(BigDecimal riktigYtelseBeløp) {
            kladd.riktigYtelseBeløp = riktigYtelseBeløp;
            return this;
        }

        public BeregningsresultatPeriode build() {
            Objects.requireNonNull(kladd.periode, "periode");
            validerPåkrevdOgScale(kladd.tilbakekrevingBeløp, "tilbakekrevingBeløp");
            validerPåkrevdOgScale(kladd.tilbakekrevingBeløpEtterSkatt, "tilbakekrevingBeløpEtterSkatt");
            validerPåkrevdOgScale(kladd.tilbakekrevingBeløpUtenRenter, "tilbakekrevingBeløpUtenRenter");
            validerPåkrevdOgScale(kladd.feilutbetaltBeløp, "periode");
            validerPåkrevdOgScale(kladd.renteBeløp, "renteBeløp");
            validerPåkrevdOgScale(kladd.skattBeløp, "skattBeløp");
            validerPåkrevdOgScale(kladd.riktigYtelseBeløp, "riktigYtelseBeløp");
            validerPåkrevdOgScale(kladd.utbetaltYtelseBeløp, "utbetaltYtelseBeløp");
            validerScale(kladd.renterProsent, "renterProsent");
            Objects.requireNonNull(this.erForeldet, "erForeldet");
            return kladd;
        }

        static void validerPåkrevdOgScale(BigDecimal beløp, String felt) {
            if (beløp == null) {
                throw new IllegalArgumentException(felt + " er påkrevd");
            }
            validerScale(beløp, felt);
        }

        static void validerScale(BigDecimal beløp, String felt) {
            if (beløp != null && beløp.scale() > 2) {
                throw new IllegalArgumentException(felt + " har mer enn 2 desimaler");
            }
        }
    }
}
