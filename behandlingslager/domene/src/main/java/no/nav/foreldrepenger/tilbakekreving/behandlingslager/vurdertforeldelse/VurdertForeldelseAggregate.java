package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse;

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

import no.nav.vedtak.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VurdertForeldelseAggregate")
@Table(name = "GR_VURDERT_FORELDELSE")
class VurdertForeldelseAggregate extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_VURDERT_FORELDELSE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vurdert_foreldelse_id", nullable = false, unique = true)
    private VurdertForeldelse vurdertForeldelse;

    @Column(name = "behandling_id", nullable = false, unique = true)
    private Long behandlingId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    private VurdertForeldelseAggregate() {
        // For hibernate
    }

    public Long getId() {
        return id;
    }

    public VurdertForeldelse getVurdertForeldelse() {
        return vurdertForeldelse;
    }

    public Long getBehandlingId() {
        return behandlingId;
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
        private VurdertForeldelseAggregate kladd = new VurdertForeldelseAggregate();

        public Builder medVurdertForeldelse(VurdertForeldelse vurdertForeldelse) {
            this.kladd.vurdertForeldelse = vurdertForeldelse;
            return this;
        }

        public Builder medBehandlingId(Long behandlingId) {
            this.kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medAktiv(boolean aktiv) {
            this.kladd.aktiv = aktiv;
            return this;
        }

        public VurdertForeldelseAggregate build() {
            Objects.requireNonNull(this.kladd.behandlingId);
            Objects.requireNonNull(this.kladd.vurdertForeldelse);
            return kladd;
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id //$NON-NLS-1$
            + ", behandlingId=" + behandlingId //$NON-NLS-1$
            + ", aktiv=" + aktiv //$NON-NLS-1$
            + ">"; //$NON-NLS-1$

    }
}
