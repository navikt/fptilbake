package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VilkårVurderingAggregate")
@Table(name = "GR_VILKAAR")
public class VilkårVurderingAggregateEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_VILKAAR")
    private Long id;

    @Column(name = "behandling_id", nullable = false, updatable = false)
    private Long behandlingId;

    @OneToOne(optional = false)
    @JoinColumn(name = "manuell_vilkaar_id", nullable = false, updatable = false)
    private VilkårVurderingEntitet manuellVilkår;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    VilkårVurderingAggregateEntitet() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public VilkårVurderingEntitet getManuellVilkår() {
        return manuellVilkår;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public void disable() {
        this.aktiv = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VilkårVurderingAggregateEntitet kladd = new VilkårVurderingAggregateEntitet();

        public Builder medBehandlingId(Long behandlingId) {
            this.kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medManuellVilkår(VilkårVurderingEntitet manuellVilkår) {
            this.kladd.manuellVilkår = manuellVilkår;
            return this;
        }

        public Builder medAktiv(boolean aktiv) {
            this.kladd.aktiv = aktiv;
            return this;
        }

        public VilkårVurderingAggregateEntitet build() {
            Objects.requireNonNull(this.kladd.behandlingId, "behandlingId");
            return kladd;
        }
    }
}
