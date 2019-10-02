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

@Entity(name = "FaktaFeilutbetalingAggregate")
@Table(name = "GR_FAKTA_FEILUTBETALING")
public class FaktaFeilutbetalingAggregate extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_FAKTA_FEILUTBETALING")
    private Long id;

    @Column(name = "behandling_id", nullable = false, updatable = false)
    private Long behandlingId;

    @OneToOne(optional = false)
    @JoinColumn(name = "fakta_feilutbetaling_id", nullable = false, updatable = false)
    private FaktaFeilutbetaling faktaFeilutbetaling;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    FaktaFeilutbetalingAggregate() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public FaktaFeilutbetaling getFaktaFeilutbetaling() {
        return faktaFeilutbetaling;
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
        private FaktaFeilutbetalingAggregate kladd = new FaktaFeilutbetalingAggregate();

        public Builder medBehandlingId(Long behandlingId) {
            this.kladd.behandlingId = behandlingId;
            return this;
        }

        public Builder medFeilutbetaling(FaktaFeilutbetaling faktaFeilutbetaling) {
            this.kladd.faktaFeilutbetaling = faktaFeilutbetaling;
            return this;
        }

        public FaktaFeilutbetalingAggregate build() {
            return kladd;
        }
    }
}
