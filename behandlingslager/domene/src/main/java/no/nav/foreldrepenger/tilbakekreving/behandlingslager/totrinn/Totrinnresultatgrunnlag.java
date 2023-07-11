package no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Totrinnresultatgrunnlag")
@Table(name = "TOTRINNRESULTATGRUNNLAG")
public class Totrinnresultatgrunnlag extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TOTRINNRESULTATGRUNNLAG")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @Column(name = "fakta_feilutbetaling_id", nullable = false, updatable = false)
    private Long faktaFeilutbetalingId;

    @Column(name = "vurdert_foreldelse_id", updatable = false)
    private Long vurderForeldelseId;

    @Column(name = "vurdert_vilkaar_id", updatable = false)
    private Long vurderVilkårId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Totrinnresultatgrunnlag() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public Long getFaktaFeilutbetalingId() {
        return faktaFeilutbetalingId;
    }

    public Long getVurderForeldelseId() {
        return vurderForeldelseId;
    }

    public Long getVurderVilkårId() {
        return vurderVilkårId;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public void disable() {
        this.aktiv = false;
    }

    public long getVersjon() {
        return versjon;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Totrinnresultatgrunnlag kladd = new Totrinnresultatgrunnlag();

        public Builder medBehandling(Behandling behandling) {
            this.kladd.behandling = behandling;
            return this;
        }

        public Builder medFeilutbetalingId(Long feilutbetalingId) {
            this.kladd.faktaFeilutbetalingId = feilutbetalingId;
            return this;
        }

        public Builder medForeldelseId(Long foreldelseId) {
            this.kladd.vurderForeldelseId = foreldelseId;
            return this;
        }

        public Builder medVilkårId(Long vilkårId) {
            this.kladd.vurderVilkårId = vilkårId;
            return this;
        }

        public Totrinnresultatgrunnlag build() {
            return kladd;
        }
    }

}
