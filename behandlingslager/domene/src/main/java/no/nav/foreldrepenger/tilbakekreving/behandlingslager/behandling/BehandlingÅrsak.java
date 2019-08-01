package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "BehandlingÅrsak")
@Table(name = "BEHANDLING_ARSAK")
public class BehandlingÅrsak extends BaseEntitet {

    @Id
    @SequenceGenerator(name = "behandling_aarsak_sekvens", sequenceName = "SEQ_BEHANDLING_ARSAK")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "behandling_aarsak_sekvens")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "behandling_arsak_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BehandlingÅrsakType.DISCRIMINATOR
            + "'"))})
    private BehandlingÅrsakType behandlingÅrsakType = BehandlingÅrsakType.UDEFINERT;

    @OneToOne
    @JoinColumn(name = "original_behandling_id", updatable = false)
    private Behandling originalBehandling;

    BehandlingÅrsak() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandling.getId();
    }

    public BehandlingÅrsakType getBehandlingÅrsakType() {
        return behandlingÅrsakType;
    }

    public Optional<Behandling> getOriginalBehandling() {
        return Optional.ofNullable(originalBehandling);
    }

    public static Builder builder(BehandlingÅrsakType behandlingÅrsakType) {
        return new Builder(behandlingÅrsakType);
    }


    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    public static class Builder {

        private BehandlingÅrsakType behandlingÅrsakType;
        private Behandling originalBehandling;

        public Builder(BehandlingÅrsakType behandlingÅrsakType) {
            Objects.requireNonNull(behandlingÅrsakType, "behandlingÅrsakTyper");
            this.behandlingÅrsakType = behandlingÅrsakType;
        }

        public Builder medOriginalBehandling(Behandling originalBehandling) {
            this.originalBehandling = originalBehandling;
            return this;
        }

        public List<BehandlingÅrsak> buildFor(Behandling behandling) {
            Objects.requireNonNull(behandling, "behandling");
            BehandlingÅrsak årsak = new BehandlingÅrsak();
            årsak.behandling = behandling;
            årsak.behandlingÅrsakType = behandlingÅrsakType;
            årsak.originalBehandling = this.originalBehandling;
            behandling.leggTilBehandlingÅrsaker(årsak);
            return behandling.getBehandlingÅrsaker();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BehandlingÅrsak that = (BehandlingÅrsak) o;

        return Objects.equals(behandlingÅrsakType, that.behandlingÅrsakType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(behandlingÅrsakType);
    }
}
