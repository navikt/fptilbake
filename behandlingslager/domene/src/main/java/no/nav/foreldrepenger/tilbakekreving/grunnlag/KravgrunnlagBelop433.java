package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import java.math.BigDecimal;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

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

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "klasse_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KlasseType.DISCRIMINATOR + "'"))
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

    public String getKlasseKode(){
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KravgrunnlagBelop433 that = (KravgrunnlagBelop433) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(klasseKode, that.klasseKode) &&
            Objects.equals(klasseType, that.klasseType) &&
            Objects.equals(nyBelop, that.nyBelop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, klasseKode, klasseType, nyBelop);
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
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
            + "klasseKode=" + klasseKode + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "klasseType=" + klasseType + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "opprUtbetBelop=" + opprUtbetBelop + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "nyBelop=" + nyBelop + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "tilbakekrevesBelop=" + tilbakekrevesBelop + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "uinnkrevdBelop=" + uinnkrevdBelop + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "skattProsent=" + skattProsent + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "resultatKode=" + resultatKode + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "årsakKode=" + årsakKode + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "skyldKode=" + skyldKode + "," //$NON-NLS-1$ //$NON-NLS-2$
            + ">";
    }
}
