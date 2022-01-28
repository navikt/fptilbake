package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;

@Entity(name = "VilkårVurderingSærligGrunn")
@Table(name = "VILKAAR_SAERLIG_GRUNN")
public class VilkårVurderingSærligGrunnEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILKAAR_SAERLIG_GRUNN")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vurder_aktsomhet_id", nullable = false, updatable = false)
    private VilkårVurderingAktsomhetEntitet vurdertAktsomhet;

    @Convert(converter = SærligGrunn.KodeverdiConverter.class)
    @Column(name = "saerlig_grunn")
    private SærligGrunn grunn;

    @Column(name = "begrunnelse")
    private String begrunnelse;

    VilkårVurderingSærligGrunnEntitet() {
        // for hibernate
    }

    public VilkårVurderingSærligGrunnEntitet(VilkårVurderingAktsomhetEntitet vurdertAktsomhet, SærligGrunn grunn) {
        this.vurdertAktsomhet = vurdertAktsomhet;
        this.grunn = grunn;
    }

    public Long getId() {
        return id;
    }

    public VilkårVurderingAktsomhetEntitet getVurdertAktsomhet() {
        return vurdertAktsomhet;
    }

    public SærligGrunn getGrunn() {
        return grunn;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VilkårVurderingSærligGrunnEntitet that = (VilkårVurderingSærligGrunnEntitet) o;
        return grunn == that.grunn
                && Objects.equals(begrunnelse, that.begrunnelse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grunn, begrunnelse);
    }

    public static class Builder {
        private VilkårVurderingSærligGrunnEntitet kladd = new VilkårVurderingSærligGrunnEntitet();

        public Builder medVurdertAktsomhet(VilkårVurderingAktsomhetEntitet vurdertAktsomhet) {
            this.kladd.vurdertAktsomhet = vurdertAktsomhet;
            return this;
        }

        public Builder medGrunn(SærligGrunn grunn) {
            this.kladd.grunn = grunn;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            this.kladd.begrunnelse = begrunnelse;
            return this;
        }

        public VilkårVurderingSærligGrunnEntitet build() {
            Objects.requireNonNull(this.kladd.vurdertAktsomhet, "vurdertAktsomhet");
            Objects.requireNonNull(this.kladd.grunn, "grunn");
            if (this.kladd.grunn == SærligGrunn.ANNET) {
                Objects.requireNonNull(this.kladd.begrunnelse, "begrunnelse kan ikke være null hvis grunn er Annet");
            }
            return kladd;
        }
    }
}
