package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "KravgrunnlagAggregateEntity")
@Table(name = "GR_KRAV_GRUNNLAG")
class KravgrunnlagAggregateEntity extends BaseEntitet implements KravgrunnlagAggregate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_KRAV_GRUNNLAG")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grunnlag_okonomi_id", nullable = false, unique = true)
    private Kravgrunnlag431 grunnlagØkonomi;

    @Column(name = "behandling_id", nullable = false, unique = true)
    private Long behandlingId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "sperret")
    private Boolean sperret = false;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    KravgrunnlagAggregateEntity() {
        // Hibernate
    }

    public KravgrunnlagAggregateEntity(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Kravgrunnlag431 getGrunnlagØkonomi() {
        return grunnlagØkonomi;
    }

    @Override
    public Long getBehandlingId() {
        return behandlingId;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public void disable() {
        this.aktiv = false;
    }

    @Override
    public boolean isSperret() {
        return sperret != null && sperret;
    }

    public void sperr() {
        this.sperret = true;
    }

    public void opphev() {
        this.sperret = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private KravgrunnlagAggregateEntity kladd = new KravgrunnlagAggregateEntity();

        public Builder medBehandlingId(Long behandlingId) {
            this.kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medGrunnlagØkonomi(Kravgrunnlag431 grunnlagØkonomi) {
            this.kladd.grunnlagØkonomi = grunnlagØkonomi;
            return this;
        }

        public Builder medAktiv(boolean aktiv) {
            this.kladd.aktiv = aktiv;
            return this;
        }

        public KravgrunnlagAggregateEntity build() {
            Objects.requireNonNull(this.kladd.behandlingId);
            Objects.requireNonNull(this.kladd.grunnlagØkonomi);
            return kladd;
        }
    }
}
