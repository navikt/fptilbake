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

import no.nav.vedtak.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "KravgrunnlagAggregate")
@Table(name = "GR_KRAV_GRUNNLAG")
public class KravgrunnlagAggregate extends BaseEntitet {

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

    KravgrunnlagAggregate() {
        // Hibernate
    }

    public KravgrunnlagAggregate(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public Long getId() {
        return id;
    }

    public Kravgrunnlag431 getGrunnlagØkonomi() {
        return grunnlagØkonomi;
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

    public Boolean isSperret() {
        return sperret;
    }

    public void sperr(){
        this.sperret = true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private KravgrunnlagAggregate kladd = new KravgrunnlagAggregate();

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

        public KravgrunnlagAggregate build() {
            Objects.requireNonNull(this.kladd.behandlingId);
            Objects.requireNonNull(this.kladd.grunnlagØkonomi);
            return kladd;
        }
    }
}
