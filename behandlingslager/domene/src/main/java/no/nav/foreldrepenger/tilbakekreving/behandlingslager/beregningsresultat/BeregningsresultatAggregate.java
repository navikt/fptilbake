package no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat;

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

@Entity(name = "BeregningsresultatAggregate")
@Table(name = "GR_BEREGNINGSRESULTAT")
public class BeregningsresultatAggregate extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_BEREGNINGSRESULTAT")
    private Long id;

    @Column(name = "behandling_id", updatable = false, nullable = false)
    private Long behandlingId;

    @ManyToOne
    @JoinColumn(name = "beregningsresultat_id", updatable = false)
    private BeregningsresultatEntitet beregningsresultat;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected BeregningsresultatAggregate() {
    }

    public BeregningsresultatAggregate(Long behandlingId, BeregningsresultatEntitet beregningsresultat) {
        Objects.requireNonNull(behandlingId, "behandlingId");
        Objects.requireNonNull(beregningsresultat, "beregningsresultat");
        this.behandlingId = behandlingId;
        this.beregningsresultat = beregningsresultat;
    }

    public BeregningsresultatEntitet getBeregningsresultat() {
        return beregningsresultat;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }
}
