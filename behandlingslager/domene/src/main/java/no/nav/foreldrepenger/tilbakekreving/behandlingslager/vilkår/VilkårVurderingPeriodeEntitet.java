package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.NavOppfulgt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

@Entity(name = "VilkårVurderingPeriode")
@Table(name = "VILKAAR_PERIODE")
public class VilkårVurderingPeriodeEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILKAAR_PERIODE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vilkaar_id", nullable = false, updatable = false)
    private VilkårVurderingEntitet vurderinger;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fom", column = @Column(name = "fom", nullable = false, updatable = false)),
        @AttributeOverride(name = "tom", column = @Column(name = "tom", nullable = false, updatable = false))
    })
    private Periode periode;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + NavOppfulgt.DISCRIMINATOR + "'", referencedColumnName = "kodeverk")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "nav_oppfulgt", referencedColumnName = "kode")),
    })
    private NavOppfulgt navOppfulgt = NavOppfulgt.UDEFINERT;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VilkårResultat.DISCRIMINATOR + "'", referencedColumnName = "kodeverk")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "vilkaar_resultat", referencedColumnName = "kode")),
    })
    private VilkårResultat vilkårResultat = VilkårResultat.UDEFINERT;

    @Column(name = "begrunnelse", nullable = false, updatable = false)
    private String begrunnelse;

    @OneToOne(mappedBy = "periode")
    private VilkårVurderingAktsomhetEntitet aktsomhet;

    @OneToOne(mappedBy = "periode")
    private VilkårVurderingGodTroEntitet godTro;

    VilkårVurderingPeriodeEntitet() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public VilkårVurderingEntitet getVurderinger() {
        return vurderinger;
    }

    public Periode getPeriode() {
        return periode;
    }

    public LocalDate getFom(){
        return periode.getFom();
    }

    public NavOppfulgt getNavOppfulgt() {
        return navOppfulgt;
    }

    public VilkårResultat getVilkårResultat() {
        return vilkårResultat;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public VilkårVurderingAktsomhetEntitet getAktsomhet() {
        return aktsomhet;
    }

    public VilkårVurderingGodTroEntitet getGodTro() {
        return godTro;
    }

    public void setAktsomhet(VilkårVurderingAktsomhetEntitet aktsomhet) {
        if (this.godTro != null) {
            throw new IllegalArgumentException("Når " + this.vilkårResultat.getKode() + " er valgt, skal ikke godTro-entiten legges til");
        }
        this.aktsomhet = aktsomhet;
    }

    public void setGodTro(VilkårVurderingGodTroEntitet godTro) {
        if (this.aktsomhet != null) {
            throw new IllegalArgumentException("Når GOD_TRO er valgt, skal ikke aktsomhet-entiten legges til");
        }
        this.godTro = godTro;
    }

    public BigDecimal finnManueltBeløp() {
        VilkårVurderingAktsomhetEntitet aktsomhetEntitet = getAktsomhet();
        if (aktsomhetEntitet != null) {
            return aktsomhetEntitet.getManueltTilbakekrevesBeløp();
        }
        VilkårVurderingGodTroEntitet godTroEntitet = getGodTro();
        if (godTroEntitet != null) {
            return godTroEntitet.getBeløpTilbakekreves();
        }
        return null;
    }

    public BigDecimal finnAndelTilbakekreves() {
        return aktsomhet != null ? aktsomhet.getProsenterSomTilbakekreves() : null;
    }

    public Boolean tilbakekrevesSmåbeløp() {
        return aktsomhet != null ? aktsomhet.getTilbakekrevSmåBeløp() : null;
    }

    public Boolean manueltSattIleggRenter() {
        return aktsomhet != null ? aktsomhet.getIleggRenter() : null;
    }

    public Boolean erBeløpIBehold() {
        return godTro != null ? godTro.isBeløpErIBehold() : null;
    }

    public Aktsomhet getAktsomhetResultat() {
        return aktsomhet != null ? aktsomhet.getAktsomhet() : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBegrunnelseAktsomhet() {
        if (aktsomhet != null) {
            return aktsomhet.getBegrunnelse();
        }
        if (godTro != null) {
            return godTro.getBegrunnelse();
        }
        return null;
    }

    public String getBegrunnelseSærligGrunner() {
        if(aktsomhet != null && !aktsomhet.getSærligGrunner().isEmpty()) {
            return aktsomhet.getSærligGrunnerBegrunnelse();
        }
        return null;
    }


    public static class Builder {
        private VilkårVurderingPeriodeEntitet kladd = new VilkårVurderingPeriodeEntitet();

        public Builder medVurderinger(VilkårVurderingEntitet vurderinger) {
            this.kladd.vurderinger = vurderinger;
            return this;
        }

        public Builder medPeriode(Periode tidsperiode) {
            this.kladd.periode = tidsperiode;
            return this;
        }

        public Builder medPeriode(LocalDate fom, LocalDate tom) {
            this.kladd.periode = Periode.of(fom, tom);
            return this;
        }

        public Builder medNavOppfulgt(NavOppfulgt navOppfulgt) {
            this.kladd.navOppfulgt = navOppfulgt;
            return this;
        }

        public Builder medVilkårResultat(VilkårResultat vilkårResultat) {
            this.kladd.vilkårResultat = vilkårResultat;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            this.kladd.begrunnelse = begrunnelse;
            return this;
        }

        public VilkårVurderingPeriodeEntitet build() {
            Objects.requireNonNull(this.kladd.periode, "periode");
            Objects.requireNonNull(this.kladd.vilkårResultat, "vilkårResultat");
            Objects.requireNonNull(this.kladd.begrunnelse, "begrunnelse");

            return kladd;
        }
    }
}
