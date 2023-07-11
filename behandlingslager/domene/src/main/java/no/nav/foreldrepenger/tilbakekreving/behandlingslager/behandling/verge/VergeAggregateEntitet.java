package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge;

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

@Entity(name = "VergeAggregateEntitet")
@Table(name = "GR_VERGE")
public class VergeAggregateEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_VERGE")
    private Long id;

    @Column(name = "behandling_id", nullable = false, updatable = false)
    private Long behandlingId;

    @OneToOne(optional = false)
    @JoinColumn(name = "verge_id", nullable = false, updatable = false)
    private VergeEntitet vergeEntitet;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    VergeAggregateEntitet() {
        //for hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public VergeEntitet getVergeEntitet() {
        return vergeEntitet;
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
        private VergeAggregateEntitet kladd = new VergeAggregateEntitet();

        public Builder medBehandlingId(Long behandlingId) {
            this.kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medVergeEntitet(VergeEntitet vergeEntitet) {
            this.kladd.vergeEntitet = vergeEntitet;
            return this;
        }

        public VergeAggregateEntitet build() {
            Objects.requireNonNull(this.kladd.behandlingId, "behandlingId");
            Objects.requireNonNull(this.kladd.vergeEntitet, "vergeEntitet");
            return kladd;
        }
    }
}
