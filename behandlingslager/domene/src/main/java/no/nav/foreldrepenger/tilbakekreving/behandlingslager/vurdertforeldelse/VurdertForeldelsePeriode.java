package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse;

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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "VurdertForeldelsePeriode")
@Table(name = "FORELDELSE_PERIODE")
public class VurdertForeldelsePeriode extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FORELDELSE_PERIODE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vurdert_foreldelse_id", nullable = false, updatable = false)
    private VurdertForeldelse vurdertForeldelse;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fom", column = @Column(name = "fom", nullable = false, updatable = false)),
        @AttributeOverride(name = "tom", column = @Column(name = "tom", nullable = false, updatable = false))
    })
    private Periode periode;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "foreldelse_vurdering_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ForeldelseVurderingType.DISCRIMINATOR + "'"))
    private ForeldelseVurderingType foreldelseVurderingType;

    @Column(name = "begrunnelse", nullable = false)
    private String begrunnelse;


    VurdertForeldelsePeriode() {
        // For hibernate
    }

    public Long getId() {
        return id;
    }

    public VurdertForeldelse getVurdertForeldelse() {
        return vurdertForeldelse;
    }

    public boolean erForeldet() {
        return ForeldelseVurderingType.FORELDET.equals(foreldelseVurderingType);
    }

    public Periode getPeriode() {
        return periode;
    }

    public LocalDate getFom() {
        return periode.getFom();
    }

    public ForeldelseVurderingType getForeldelseVurderingType() {
        return foreldelseVurderingType;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VurdertForeldelsePeriode kladd = new VurdertForeldelsePeriode();

        public Builder medVurdertForeldelse(VurdertForeldelse vurdertForeldelse) {
            this.kladd.vurdertForeldelse = vurdertForeldelse;
            return this;
        }

        public Builder medPeriode(Periode periode) {
            this.kladd.periode = periode;
            return this;
        }

        public Builder medPeriode(LocalDate fom, LocalDate tom) {
            this.kladd.periode = Periode.of(fom, tom);
            return this;
        }


        public Builder medForeldelseVurderingType(ForeldelseVurderingType foreldelseVurderingType) {
            this.kladd.foreldelseVurderingType = foreldelseVurderingType;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            this.kladd.begrunnelse = begrunnelse;
            return this;
        }

        public VurdertForeldelsePeriode build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id //$NON-NLS-1$
            + ", periode=" + periode //$NON-NLS-1$
            + ", foreldelseVurderingType=" + foreldelseVurderingType //$NON-NLS-1$
            + ">"; //$NON-NLS-1$

    }
}
