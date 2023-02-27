package no.nav.foreldrepenger.tilbakekreving.grunnlag;

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

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

@Entity(name = "KravgrunnlagBelop433")
@Table(name = "KRAV_GRUNNLAG_BELOP_433")
public class KravgrunnlagBelop433 extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KRAV_GRUNNLAG_BELOP_433")
    private Long id;

    @Column(name = "klasse_kode")
    private String klasseKode;

    @Convert(converter = KlasseType.KodeverdiConverter.class)
    @Column(name = "klasse_type", nullable = false, updatable = false)
    private KlasseType klasseType;

    @Column(name = "oppr_utbet_belop")
    private BigDecimal opprUtbetBelop = BigDecimal.ZERO;

    @Column(name = "ny_belop", nullable = false, updatable = false)
    private BigDecimal nyBelop = BigDecimal.ZERO;

    @Column(name = "tilbake_kreves_belop")
    private BigDecimal tilbakekrevesBelop = BigDecimal.ZERO;

    @Column(name = "uinnkrevd_belop")
    private BigDecimal uinnkrevdBelop = BigDecimal.ZERO;

    @Column(name = "skatt_prosent")
    private BigDecimal skattProsent = BigDecimal.ZERO;

    @Column(name = "resultat_kode")
    private String resultatKode;

    @Column(name = "aarsak_kode")
    private String årsakKode;

    @Column(name = "skyld_kode")
    private String skyldKode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "krav_grunnlag_periode_432_id", nullable = false, updatable = false)
    private KravgrunnlagPeriode432 kravgrunnlagPeriode432;

    public Long getId() {
        return id;
    }

    public KlasseKode getKlasseKodeKodeverk() {
        return KlasseKode.fraKode(klasseKode);
    }

    public String getKlasseKode() {
        return klasseKode;
    }

    public KlasseType getKlasseType() {
        return klasseType;
    }

    public BigDecimal getOpprUtbetBelop() {
        return opprUtbetBelop;
    }

    public BigDecimal getNyBelop() {
        return nyBelop;
    }

    public BigDecimal getTilbakekrevesBelop() {
        return tilbakekrevesBelop;
    }

    public BigDecimal getUinnkrevdBelop() {
        return uinnkrevdBelop;
    }

    public BigDecimal getSkattProsent() {
        return skattProsent;
    }

    public String getResultatKode() {
        return resultatKode;
    }

    public String getÅrsakKode() {
        return årsakKode;
    }

    public String getSkyldKode() {
        return skyldKode;
    }

    public KravgrunnlagPeriode432 getKravgrunnlagPeriode432() {
        return kravgrunnlagPeriode432;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KravgrunnlagBelop433 that = (KravgrunnlagBelop433) o;
        return Objects.equals(klasseKode, that.klasseKode) &&
            klasseType == that.klasseType &&
            erBigDecimalLik(opprUtbetBelop, that.opprUtbetBelop) &&
            erBigDecimalLik(nyBelop, that.nyBelop) &&
            erBigDecimalLik(tilbakekrevesBelop, that.tilbakekrevesBelop) &&
            erBigDecimalLik(uinnkrevdBelop, that.uinnkrevdBelop) &&
            erBigDecimalLik(skattProsent, that.skattProsent) &&
            Objects.equals(resultatKode, that.resultatKode) &&
            Objects.equals(årsakKode, that.årsakKode) &&
            Objects.equals(skyldKode, that.skyldKode);
    }

    private boolean erBigDecimalLik(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null && bd2 == null)
            return true;
        if (bd1 == null || bd2 == null)
            return false;
        return bd1.compareTo(bd2) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(klasseKode, klasseType, opprUtbetBelop, nyBelop, tilbakekrevesBelop, uinnkrevdBelop, skattProsent, resultatKode, årsakKode, skyldKode);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private KravgrunnlagBelop433 kladd = new KravgrunnlagBelop433();


        public Builder medKlasseKode(KlasseKode klasseKode) {
            this.kladd.klasseKode = klasseKode.getKode();
            return this;
        }

        public Builder medKlasseKode(String klasseKode) {
            this.kladd.klasseKode = klasseKode;
            return this;
        }

        public Builder medKlasseType(KlasseType klasseType) {
            this.kladd.klasseType = klasseType;
            return this;
        }

        public Builder medOpprUtbetBelop(BigDecimal opprUtbetBelop) {
            this.kladd.opprUtbetBelop = opprUtbetBelop;
            return this;
        }

        public Builder medNyBelop(BigDecimal nyBelop) {
            this.kladd.nyBelop = nyBelop;
            return this;
        }

        public Builder medTilbakekrevesBelop(BigDecimal tilbakekrevesBelop) {
            this.kladd.tilbakekrevesBelop = tilbakekrevesBelop;
            return this;
        }

        public Builder medUinnkrevdBelop(BigDecimal uinnkrevdBelop) {
            this.kladd.uinnkrevdBelop = uinnkrevdBelop;
            return this;
        }

        public Builder medSkattProsent(BigDecimal skattProsent) {
            this.kladd.skattProsent = skattProsent;
            return this;
        }

        public Builder medResultatKode(String resultatKode) {
            this.kladd.resultatKode = resultatKode;
            return this;
        }

        public Builder medÅrsakKode(String årsakKode) {
            this.kladd.årsakKode = årsakKode;
            return this;
        }

        public Builder medSkyldKode(String skyldKode) {
            this.kladd.skyldKode = skyldKode;
            return this;
        }

        public Builder medKravgrunnlagPeriode432(KravgrunnlagPeriode432 kravgrunnlagPeriode432) {
            this.kladd.kravgrunnlagPeriode432 = kravgrunnlagPeriode432;
            return this;
        }

        public KravgrunnlagBelop433 build() {
            Objects.requireNonNull(this.kladd.klasseKode, "klasseKode");
            Objects.requireNonNull(this.kladd.klasseType, "klasseType");
            Objects.requireNonNull(this.kladd.nyBelop, "nyBelop");
            return kladd;
        }
    }

    @Override
    public String toString() {
        return "KravgrunnlagBelop433{" +
            "klasseKode='" + klasseKode + '\'' +
            ", klasseType=" + klasseType +
            ", opprUtbetBelop=" + opprUtbetBelop +
            ", nyBelop=" + nyBelop +
            ", tilbakekrevesBelop=" + tilbakekrevesBelop +
            ", uinnkrevdBelop=" + uinnkrevdBelop +
            ", skattProsent=" + skattProsent +
            ", resultatKode='" + resultatKode + '\'' +
            ", årsakKode='" + årsakKode + '\'' +
            ", skyldKode='" + skyldKode + '\'' +
            '}';
    }
}
