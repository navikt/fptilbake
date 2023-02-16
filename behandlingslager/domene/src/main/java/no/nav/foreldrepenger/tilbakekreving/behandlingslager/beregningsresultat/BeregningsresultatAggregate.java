package no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat;

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
