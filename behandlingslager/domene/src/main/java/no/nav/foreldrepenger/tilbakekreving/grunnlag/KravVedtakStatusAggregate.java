package no.nav.foreldrepenger.tilbakekreving.grunnlag;

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

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "KravVedtakStatusAggregate")
@Table(name = "GR_KRAV_VEDTAK_STATUS")
class KravVedtakStatusAggregate extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_KRAV_VEDTAK_STATUS")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "krav_vedtak_status_id", nullable = false, unique = true)
    private KravVedtakStatus437 kravVedtakStatus;

    @Column(name = "behandling_id", nullable = false, unique = true)
    private Long behandlingId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    KravVedtakStatusAggregate() {
        // Hibernate
    }

    public KravVedtakStatusAggregate(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public Long getId() {
        return id;
    }

    public KravVedtakStatus437 getKravVedtakStatus() {
        return kravVedtakStatus;
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
        private final KravVedtakStatusAggregate kladd = new KravVedtakStatusAggregate();

        public Builder medBehandlingId(Long behandlingId) {
            this.kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medKravVedtakStatus(KravVedtakStatus437 kravVedtakStatus) {
            this.kladd.kravVedtakStatus = kravVedtakStatus;
            return this;
        }

        public Builder medAktiv(boolean aktiv) {
            this.kladd.aktiv = aktiv;
            return this;
        }

        public KravVedtakStatusAggregate build() {
            Objects.requireNonNull(this.kladd.behandlingId);
            Objects.requireNonNull(this.kladd.kravVedtakStatus);
            return kladd;
        }
    }
}
