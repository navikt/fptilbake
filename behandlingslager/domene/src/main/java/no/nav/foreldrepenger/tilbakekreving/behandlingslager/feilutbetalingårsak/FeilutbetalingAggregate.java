package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

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

@Entity(name = "FeilutbetalingAggregate")
@Table(name = "GR_FEILUTBETALING")
public class FeilutbetalingAggregate extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_FEILUTBETALING")
    private Long id;

    @Column(name = "behandling_id", nullable = false, updatable = false)
    private Long behandlingId;

    @OneToOne(optional = false)
    @JoinColumn(name = "feilutbetaling_id", nullable = false, updatable = false)
    private Feilutbetaling feilutbetaling;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    FeilutbetalingAggregate() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public Feilutbetaling getFeilutbetaling() {
        return feilutbetaling;
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
        private FeilutbetalingAggregate kladd = new FeilutbetalingAggregate();

        public Builder medBehandlingId(Long behandlingId) {
            this.kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medFeilutbetaling(Feilutbetaling feilutbetaling) {
            this.kladd.feilutbetaling = feilutbetaling;
            return this;
        }

        public FeilutbetalingAggregate build() {
            return kladd;
        }
    }
}
