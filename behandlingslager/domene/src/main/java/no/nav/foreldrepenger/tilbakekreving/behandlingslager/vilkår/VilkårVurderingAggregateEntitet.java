package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VilkårVurderingAggregate")
@Table(name = "GR_VILKAAR")
class VilkårVurderingAggregateEntitet extends BaseEntitet {

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

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

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
