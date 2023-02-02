package no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

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
    private Beregningsresultat beregningsresultat;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    protected BeregningsresultatAggregate() {
    }

    public BeregningsresultatAggregate(Long behandlingId, Beregningsresultat beregningsresultat) {
        Objects.requireNonNull(behandlingId, "behandlingId");
        Objects.requireNonNull(beregningsresultat, "beregningsresultat");
        this.beregningsresultat = beregningsresultat;
    }

    public Beregningsresultat getBeregningsresultat() {
        return beregningsresultat;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }
}
